// Let's create our own strategy
var strat = {};

// Prepare everything our strat needs
strat.init = function() {
  // your code
  console.log(this.settings);

  this.addTulipIndicator('emaFast', 'ema', {
      optInTimePeriod: this.settings.fast
  });

  this.addTulipIndicator('emaSlow', 'ema', {
    optInTimePeriod: this.settings.slow
});

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
  const emaFast = this.tulipIndicators.emaFast.result.result;
  const emaSlow = this.tulipIndicators.emaSlow.result.result;
  console.log("EMAFAST: " + emaFast);
  console.log("EMASLOW: " + emaSlow);


  if(emaFast > emaSlow){
    this.advice('long');
  }
  else{
    this.advice('short');
  }
}

// Optional for executing code
// after completion of a backtest.
// This block will not execute in
// live use as a live gekko is
// never ending.
strat.end = function() {
  // your code!
}

module.exports = strat;
