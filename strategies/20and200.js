var method = {};

//GLOBALS
//Candle Storage
var candleListHead;
var candleListTail;
var maxNumberOfCandles = 25000;
var currentNumberOfCandles = 0;

var stopLoss;

var tradeStats;
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

class TradeStats{
  constructor(){
    //Trade Counters
    var risingLong = 0;
    var bouncingLong = 0;
    var fallingLong = 0;
    var stopouts = 0;
    var takeProfits = 0;
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

  tradeStats = new TradeStats();
  tradeStats.risingLong = 0;
  tradeStats.bouncingLong = 0;
  tradeStats.fallingLong = 0;
  tradeStats.stopouts = 0;
  tradeStats.takeProfits = 0;
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
        //this is a long candle. Continue on through execution.
        console.log(candle.start.format()); //Print Date
      }else{
        //This is a short check candle.
        //TODO call another method.
        return;
      }
      //Decide rising or falling 20
      if(emaDirection("FAST") >= 0){
        //FAST EMA is going up.
        var buyThreshold; //The wiggle room above EMA to buy at. Calculated as: EMA * percentage
        //Make sure rising FAST is above SLOW ("Picture of Power")
        if(candleListTail.thisFastEMA > candleListTail.thisSlowEMA){
          //Check if current price (close price) is above or below the FAST to determine whether we buy at FAST or SLOW.
          if(candle.close >= candleListTail.thisFastEMA){
            //The candle close was above the current FAST EMA Try to buy at FAST.
            buyThreshold = deltaCloseAboveEMA * candleListTail.thisFastEMA;
            //Check if close price in BUY ZONE: Buy at or near rising FAST, try to sell far above rising FAST
            if(candle.close < candleListTail.thisFastEMA + buyThreshold){ //&& (candle.low <= candle.close + buyThreshold && candle.low >= candleListTail.emaFast)){ //This was for checking tail value
              //We expect the price to explode upward.
              setStopLoss(candle);
              console.log('^^^ RISING LONG ENTERED @ ' + candle.close);
              this.advice('long');
              this.state = 'SELL_ABOVE_FAST';
              tradeStats.risingLong+= 1;
            }
            else{
              // Price above FAST but not in buy zone. Wait...
            }
          }else{

          }
        }else{
          //FAST is rising but not above 200. Do nothing?..
        }
      }else{
        //FAST EMA is going down.
        //Buy far away from falling FAST, sell @ falling FAST
        if(candle.close < candleListTail.thisFastEMA - (candleListTail.thisFastEMA * deltaFarBelowEMA)){
          //We expect the price to whip upward into FAST (snapback)
          setStopLoss(candle);
          console.log('^^^ FALLING LONG ENTERED @ ' + candle.close);
          this.advice('long');
          this.state = 'SELL_AT_FAST';
          tradeStats.fallingLong+= 1;
        }

        //The candle close was below the current FAST EMA. Try to buy at SLOW.
        buyThreshold = deltaCloseAboveEMA * candleListTail.thisSlowEMA;
        //Make sure current price (close price) is above the SLOW
        if(candle.close >= candleListTail.thisSlowEMA){
          //Check if close price in BUY ZONE: Buy at or near flat/rising SLOW, sell just below FAST before it turns down.
          if(candle.close < candleListTail.thisSlowEMA + buyThreshold){ //&& (candle.low <= candle.close + buyThreshold && candle.low >= candleListTail.emaFast)){ //This was for checking tail value
            //We expect the price to bounce off of SLOW and into FAST.
            setStopLoss(candle);
            console.log('^^^ BOUNCING LONG ENTERED @ ' + candle.close);
            this.advice('long');
            this.state = 'SELL_AT_FAST';
            tradeStats.bouncingLong+= 1;
          }
          else{
            // Price below Fast and above SLOW but not in buy zone. Wait...
          }
        }
      }
      break;
      case 'SELL_ABOVE_FAST':
        //stopLoss += 0.015;
        console.log('SELL HIGH PLZ @ ' + (candleListTail.thisFastEMA + (candleListTail.thisFastEMA * deltaFarAboveEMA)) );
        console.log('STOP LOSS: ' + stopLoss);
        if(candle.close < stopLoss){
          console.log('--- Stoploss triggered, stoploss: ' + stopLoss);
          this.advice('short');
          this.state = 'READY_TO_BUY';
          tradeStats.stopouts+= 1;
        }
        else if (candle.close >= (candleListTail.thisFastEMA + (candleListTail.thisFastEMA * deltaFarAboveEMA))) {
          console.log('+++ Sold at target above fast. Close price: ' + (candleListTail.thisFastEMA + (candleListTail.thisFastEMA * deltaFarAboveEMA)));
          this.advice('short');
          this.state = 'READY_TO_BUY';
          tradeStats.takeProfits+= 1;
        }
        break;
      case 'SELL_AT_FAST':
      //stopLoss += 0.015;
      if(candle.close < stopLoss){
        console.log('--- Stoploss triggered , stoploss: ' + stopLoss);
        this.advice('short');
        this.state = 'READY_TO_BUY';
        tradeStats.stopouts+= 1;
      }else if(candle.close > (candleListTail.thisFastEMA - deltaCloseBelowEMA * candleListTail.thisFastEMA)){
        console.log('+++ Selling at fast. Close price: ' + candle.close);
        this.advice('short');
        this.state = 'READY_TO_BUY';
        tradeStats.takeProfits+= 1;
      }
      break;
  }
}

method.end = function(){
  //printStoredCandles();
  console.log(tradeStats);
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
