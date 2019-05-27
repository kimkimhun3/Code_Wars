//Is the string uppercase? OF JAVA SCRIPT

//define the string prototype here 
String.prototype.isUpperCase = function(){
  const pattern = `[A-Z .%*&#()%&^#]{${this.length}}`;
  const regex = new RegExp(pattern);
  return regex.test(this);
}
