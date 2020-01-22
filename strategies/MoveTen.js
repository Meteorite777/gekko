class LinkedListNode {
	constructor() {
		var prevCandleNode = null;
		var thisCandle = null;
		var nextCandleNode = null;
	}
}


// Let's create our own strategy
var strat = {};
var candleList = null;
var lastCandleNode = null;
var lookbackhours = null; //Lookback time period in hrs to compare
var percentagedrop = null; //Keep this number positive and as a percent (use decimal places).
var stoplossPercent = null; //Positive percentage.

var state = "READY_TO_BUY"; // "READY_TO_SELL"
var stoplossValue;

// Prepare everything our strat needs
strat.init = function() {
  // your code
	console.log("Strategy Parameters Configured:");
  console.log(this.settings);
	lookbackhours = this.settings.lookbackhours; //Lookback time period in hrs to compare
	percentagedrop = this.settings.percentagedrop; //Keep this number positive and as a percent (use decimal places).
	stoplossPercent = this.settings.stoplossPercent; //Positive percentage.
}

// What happens on every new candle?
strat.update = function(candle) {
  // your code!


}

// For debugging purposes.
strat.log = function() {
  // your code!
}

// Based on the newly calculated
// information, check if we should
// update or not.
strat.check = function(candle) {
  // your code!

	//Add candle to structure.
  linkCandle(candle);

	//console.log("State: " + state);

	switch(state){
		case "READY_TO_BUY":
		//Measure change from lookbackhours.
		//Find the max candle height from the lookback period.
		var currentDate = new Date(candle.start);
		var periodStartDate = new Date(candle.start); //Possible underflow later?
		periodStartDate.setHours(periodStartDate.getHours() - lookbackhours)
    //console.log(candle.start);
		//console.log(currentDate);
		//console.log(periodStartDate);
		//console.log(new Date(currentDate.getHours - 24));
		var maxPrice = candle.high;
		var minPrice = candle.low;

		var curNode = lastCandleNode.prevCandleNode;
		if(curNode == null){
			return; //warming up
		}

		var tmpDate = new Date(curNode.thisCandle.start);
		while(tmpDate > periodStartDate && curNode != null){
			maxPrice = Math.max(maxPrice, curNode.thisCandle.high);
			minPrice = Math.max(minPrice, curNode.thisCandle.low);

			curNode = curNode.prevCandleNode;
		}

		var percentAsValue = percentagedrop * maxPrice;
		//console.log("MinPrice: " + minPrice);
		//console.log("MaxPrice: " + maxPrice);
		if(maxPrice - percentAsValue > candle.close){
			//Check for confirmation that atleast one green candle closed before buy.
			if(lastCandleNode.prevCandleNode.thisCandle.close <= candle.close){
				//BUY
				this.advice("long");
				/*this.advice({
					direction: 'long',
					trigger: {
						type: 'traiingStop',
						trailPercentage: 2
					}
				})*/
				state = "READY_TO_SELL";
				stoplossValue = candle.close - (stoplossPercent * candle.close);
			}else{
				//Wait for a trend.
			}
		}
		break;

		case "READY_TO_SELL":
			if(stoplossValue >= candle.close){
				this.advice("short");
				candleList = null;
				lastCandleNode = null;
				state = "READY_TO_BUY";
			}

			stoplossValue = Math.max(candle.close - (stoplossPercent * candle.close), stoplossValue);

			break;
	}

}

// Optional for executing code
// after completion of a backtest.
// This block will not execute in
// live use as a live gekko is
// never ending.
strat.end = function() {
	return;
  // your code!
	var n = candleList;
	while(n != null){
		//console.log(n.thisCandle);
		console.log(n.thisCandle.close);
		n = n.nextCandleNode;
	}
	return;
	console.log(lastCandleNode.prevCandleNode);
	console.log(lastCandleNode.thisCandle);
}

function linkCandle(candle){
	if(candleList == null){
		candleList = new LinkedListNode(); //Blank node
		candleList.thisCandle = candle; //Set node values
		lastCandleNode = candleList;
		//console.log("New List");
	}else{
		const newNode = new LinkedListNode(); //blank node
		newNode.thisCandle = candle;
		newNode.prevCandleNode = lastCandleNode;
		newNode.nextCandleNode = null; //Not needed.
		lastCandleNode.nextCandleNode = newNode;
		lastCandleNode = newNode;
		//console.log("Old List");
	}
	//console.log(lastCandleNode.thisCandle);
}

module.exports = strat;
