function testForIn( x ) {
  var z = "";
  for(var y in x) {
    z += (x[y])();
  }
  return z;
}

var q = testForIn({
  foo: function testForIn1() { return 7; },
  bar: function testForIn2() { return "whatever"; }
});

assert(q == 7);

