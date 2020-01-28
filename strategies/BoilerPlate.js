var method = {};

var candleListHead;
var candleListTail;

//Linked list
class LinkedListNode{
  constructor(){
    var prevCandleNode = null;
    var thisCandle = null;
    var nextCandleNode = null;
  }
}

// Prepare everything our method needs
method.init = function() {
  this.state = 'WARMING_UP';
  this.candleList = null;
}
// What happens on every new candle?
method.update = function(candle) {
  // nothing!
}
method.log = function() {
  // nothing!
}
method.check = function(candle) {
  storeCandle(candle);

  switch(this.state){
    case 'WARMING_UP':

      break;
  }

}

method.end = function(){
  printStoredCandles();
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
}

function printStoredCandles(){
  var tmp = candleListHead;

  while(tmp != null){
    console.log(tmp.thisCandle);
    tmp = tmp.nextCandleNode;
  }
}

module.exports = method;
