var method = {};

//GLOBALS
//Candle Storage
var candleListHead;
var candleListTail;
var maxNumberOfCandles = 25000;
var currentNumberOfCandles = 0;

var stopLoss;

//Wake counter
var checkCalls = 0;

//PARAMETERS
//Candle Lengths in minutes
var checkTime;
var candleLength;

//Distance from fast ema to trigger "being close"
var deltaCloseBelowEMA;
var deltaCloseAboveEMA;
var deltaFarBelowEMA;
var deltaFarAboveEMA;

var stopLossPercent;



//Linked list
class LinkedListNode{
  constructor(){
    var prevCandleNode = null;
    var thisCandle = null;
    var thisFastEMA = null;
    var thisSlowEMA = null;
    var nextCandleNode = null;
  }
}



// Prepare everything our method needs
method.init = function() {
  this.state = 'WARMING_UP';
  this.candleList = null;

  checkTime = this.settings.checkTime;
  candleLength = this.settings.candleLength;
  deltaCloseAboveEMA = this.settings.deltaCloseAboveEMA; //Close means near. NOT END (English) FOR BUYING @ RISING FAST
  deltaCloseBelowEMA = this.settings.deltaCloseBelowEMA; //FOR TAKING PROFIT UNDER FAST
  deltaFarAboveEMA = this.settings.deltaFarAboveEMA; //FOR TAKING PROFIT ABOVE FAST
  deltaFarBelowEMA = this.settings.deltaFarBelowEMA; // FOR BUYING BELOW A FALLING FAST
  stopLossPercent = this.settings.stopLossPercent;


  this.addTulipIndicator('emaFast', 'ema', {
      optInTimePeriod: (20 * (candleLength / checkTime))
  });

  this.addTulipIndicator('emaSlow', 'ema', {
    optInTimePeriod: (200 * (candleLength / checkTime))
  });


}
// What happens on every new candle?
method.update = function(candle) {
  // nothing!
}
method.log = function() {
  // nothing!
}
method.check = function(candle) {
  checkCalls += 1;

  storeCandle(candle);
  //Store the EMA values for this candle.
  candleListTail.thisFastEMA = this.tulipIndicators.emaFast.result.result;
  candleListTail.thisSlowEMA = this.tulipIndicators.emaSlow.result.result;


  switch(this.state){
    case 'WARMING_UP':
      if(checkCalls >= (200 * (candleLength / checkTime))){
        this.state = "READY_TO_BUY";
      }
      break;
    case 'READY_TO_BUY':
      //Check if real candle or check candle.
      if(checkCalls % (candleLength /checkTime) == 0){
        //this is a long candle.
      }else{
        //This is a short check candle.
        //TODO call another method.
        return;
      }

      //Decide rising or falling 20
      if(emaDirection("FAST") >= 0){
        //EMA is going up.
        //Check if current price (close price) is above or below the fast
        var buyThreshold; //The distance above fast EMA to buy up to.
        buyThreshold = deltaCloseAboveEMA * candleListTail.thisFastEMA;

        if((candle.close > candleListTail.thisFastEMA && candle.close < candleListTail.thisFastEMA + buyThreshold)
            && (candleListTail.thisFastEMA > candleListTail.thisSlowEMA)){
            //&& (candle.low <= candle.close + buyThreshold && candle.low >= candleListTail.emaFast)){ //This was for checking tail value
          //The price is above the 20 (fast) EMA
          //Check if close is near the fast.
          var upperPrice = 1 + deltaCloseAboveEMA;
          upperPrice *= candle.close;
          setStopLoss(candle);
          console.log('LONG ENTERED @ ' + candle.close);
          this.advice('long');
          this.state = 'SELL_ABOVE_FAST';
        }else{
          //The candle close was below the current fast EMA
          //TODO buy at SLOW, sell under FAST
        }
      }else{
        //EMA is going down.
        if(candle.close < candleListTail.thisFastEMA - (candleListTail.thisFastEMA * deltaFarBelowEMA)){
          //We expect the price to whip upward (snapback)
          setStopLoss(candle);
          console.log('LONG ENTERED @ ' + candle.close);
          this.advice('long');
          this.state = 'SELL_AT_FAST';
        }
      }
      break;
      case 'SELL_ABOVE_FAST':
        //stopLoss += 0.015;
        console.log('SELL HIGH @ PLZ: ' + (candleListTail.thisFastEMA + (candleListTail.thisFastEMA * deltaFarAboveEMA)) );
        console.log('STOP LOSS: ' + stopLoss);
        if(candle.close < stopLoss){
          console.log('Stoploss triggered, stoploss: ' + stopLoss);
          this.advice('short');
          this.state = 'READY_TO_BUY';
        }
        else if (candle.close >= (candleListTail.thisFastEMA + (candleListTail.thisFastEMA * deltaFarAboveEMA))) {
          console.log('Sold at target above fast @ ' + (candleListTail.thisFastEMA + (candleListTail.thisFastEMA * deltaFarAboveEMA)));
          this.advice('short');
          this.state = 'READY_TO_BUY';
        }
        break;
      case 'SELL_AT_FAST':
      //stopLoss += 0.015;
      if(candle.close < stopLoss){
        console.log('Stoploss triggered, stoploss: ' + stopLoss);
        this.advice('short');
        this.state = 'READY_TO_BUY';
      }else if(candle.close > (candleListTail.thisFastEMA - deltaCloseBelowEMA * candleListTail.thisFastEMA)){
        console.log('Selling at fast. Close price: ' + candle.close);
        this.advice('short');
        this.state = 'READY_TO_BUY';
      }
      break;
  }
}

method.end = function(){
  //printStoredCandles();
}

function setStopLoss(candle){
  var delta = candle.close * stopLossPercent;
  stopLoss = candle.close - delta;
  return stopLoss;
}

//Returns instantanious slope of EMA.
function emaDirection(whichEMA){
  //Search for the ACTUAL last candle.
  var counter = candleLength / checkTime;
  var lastCandleNode = candleListTail;

  while(counter > 0 && lastCandleNode != null){
    counter += -1;
    lastCandleNode = lastCandleNode.prevCandleNode;
  }

  if(whichEMA.toLowerCase() == "fast"){
    return candleListTail.thisFastEMA - lastCandleNode.thisFastEMA;
  }else{
    //assume slow
    return candleListTail.thisSlowEMA - lastCandleNode.thisSlowEMA;
  }
}

function storeCandle(candle){
  //Create a new list.
  if(candleListHead == null){
    //Create the node
    candleListHead = new LinkedListNode(); //create blank node
    candleListHead.thisCandle = candle;

    candleListTail = candleListHead;
  }else{
    //Add this candle to the list.
    var tmp = new LinkedListNode();
    tmp.prevCandleNode = candleListTail;
    tmp.thisCandle = candle;
    candleListTail.nextCandleNode = tmp;

    //set the last link to this new node.
    candleListTail = tmp;
  }

  //Do not store all candles for all eternity.
  currentNumberOfCandles += 1;
  if(currentNumberOfCandles > maxNumberOfCandles){
    candleListHead = candleListHead.nextCandleNode;
    candleListHead.prevCandleNode = null;
    currentNumberOfCandles += -1;
  }
}

function printStoredCandles(){
  var tmp = candleListHead;

  while(tmp != null){
    console.log(tmp.thisCandle);
    console.log("->EMA fast, slow: " + tmp.thisFastEMA + ", " + tmp.thisSlowEMA);
    tmp = tmp.nextCandleNode;
  }
}

module.exports = method;
