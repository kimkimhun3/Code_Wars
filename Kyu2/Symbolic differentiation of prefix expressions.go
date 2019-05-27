package kata
import (
  "fmt"
  "math"
  "regexp"
  "strconv"
  "strings"
)


var opStrings = []string{  "+", "-", "*",  "/", "^", "cos", "sin", "tan", "exp", "ln", "x", "cte", ""}

type OperatorType int
const (
  SumOp OperatorType = iota
  Subtract
  Multiply
  Division
  Power
  Cos
  Sin
  Tan
  ExpOp
  Ln
  VarOp
  Constant
  Unknown
)
func (op OperatorType) String() string {
  return opStrings[op]
}

func (op OperatorType) OperandNumber() int {
  rv := 0
  switch op {
  case SumOp:
    rv = 2
  case Subtract:
    rv = 2
  case Multiply:
    rv = 2
  case Division:
    rv = 2
  case Power:
    rv = 2
  case Cos:
    rv = 1
  case Sin:
    rv = 1
  case Tan:
    rv = 1
  case ExpOp:
    rv = 1
  case Ln:
    rv = 1
  }
  return rv
}

func (op OperatorType)Execute(values []float64) (rv float64) {
  rv = 0
  switch op {
  case SumOp:
    rv = values[0] + values[1]
  case Subtract:
    rv = values[0] - values[1]
  case Division:
    rv = values[0] / values[1]
  case Multiply:
    rv = values[0] * values[1]
  case Power:
    rv = math.Pow(values[0], values[1])
  case Cos:
    rv = math.Cos(values[0])
  case Sin:
    rv = math.Sin(values[0])
  case Tan:
    rv = math.Tan(values[0])
  case ExpOp:
    rv = math.Exp(values[0])
  case Ln:
    rv = math.Log(values[0])
  }
  return
}

func getConstant(v float64) *Operation {
  return &Operation{ operator: Constant, value: v, operands: nil }
}

type OperationList []*Operation
func (l OperationList) Copy() OperationList {
  rv := make([]*Operation, len(l))
  for i, v := range l {
    rv[i] = v.Copy()
  }
  return rv
}
func (l OperationList)String() string {
  var rv []string
  for _, v := range l {
    rv = append(rv, v.String())
  }
  return strings.Join(rv, " ")
}

func (l OperationList)Simplify() {
  for _, op := range l {
    if op != nil {
      op.Simplify()
    }
  }
}

func (l OperationList) GetConstantValues() (constants []float64, complex OperationList) {
  for _, v := range l {
    if v.operator == Constant {
      constants = append(constants, v.value)
    } else {
      complex = append(complex, v)
    }
  }
  return
}

type Operation struct {
  operator OperatorType
  value    float64
  operands OperationList
}

func (o *Operation)String() string {
  if o.operator == Constant {
    return fmt.Sprintf("%.12g", o.value)
  } else if o.operator == VarOp {
    return o.operator.String()
  }
  return fmt.Sprintf("(%v %+v)", o.operator, o.operands)
}

func (o *Operation)Copy() *Operation {
  return &Operation{
    operator: o.operator,
    value: o.value,
    operands: o.operands.Copy(),
  }
}

// The operators and functions you are required to implement are + - * / ^ cos sin tan exp ln
// where ^ means raised to power of.
// exp is the exponential function (same as e^x) and ln is the natural logarithm (base e).
func (o *Operation) Derive() *Operation {
  o.operator.DeriveMethod()(o)
  return o
}

// Recursively simplifies a (complex) operation by doing the following:
// - If both operands are Constants, execute the operation and replace the operation with a constant
// - If the operation is F * 1, or 1 * F, replace with F
// - If the operation is F + 0 or 0 + F replace with F
// - If the operation is F / 1, replace with F
// - If the operation is F - 0, replace with F
// - If the operation is F ^ 1, replace with F
// - If the operation is F ^ 0, replace with 1
func (o *Operation)Simplify() *Operation{
  if o.operator == Constant || o.operator == VarOp {
    return o
  }
  o.operands.Simplify()
  if constants, complex := o.operands.GetConstantValues(); len(complex) == 0  {
    o.value = o.operator.Execute(constants)
    o.operator = Constant
  } else if len(constants) == 1 { // We only have unary and binary operators, so if this is true, len(complex) is 1 also
    val := constants[0]
    shouldReplace := o.operator == Multiply && val == 1 || o.operator == SumOp && val == 0
    switch o.operator {
    case Multiply:
      if val == 0 {
        shouldReplace = true
        complex[0] = getConstant(0)
      }
    case Subtract:
      shouldReplace = val == 0 && o.operands[0] == complex[0]
    case Division:
      shouldReplace = val == 1 && o.operands[0] == complex[0]
    case Power:
      shouldReplace = val == 1 && o.operands[0] == complex[0]
      if val == 0 {
        shouldReplace = true
        complex[0] = getConstant(1)
      }
    }

    if shouldReplace {
      o.operator = complex[0].operator
      o.operands = complex[0].operands
    }

  }

  return o
}


// Rewriting rules
// (+ A B)   => (+ Derive(A) Derive(B))
func sumDerive(o *Operation) {
  for _, op := range o.operands {
    op.Derive()
  }
}

// (* A B)   => (+ (* Derive(A) B) (* A Derive(B))
func multiplyDerive(o *Operation) {
  a := o.operands[0]
  b := o.operands[1]
  dA := a.Copy().Derive()
  dB := b.Copy().Derive()
  o.operator = SumOp
  o.operands[0] = &Operation{
    operator: Multiply,
    operands: OperationList{dA, b},
  }
  o.operands[1] = &Operation{
    operator: Multiply,
    operands: OperationList{a, dB},
  }
}

// (/ A B) => (/ (- (* Derive(A) B) (* A Derive(B))) (^ B 2))
func divideDerive(o *Operation) {
  a := o.operands[0]
  b := o.operands[1]
  dA := a.Copy().Derive()
  dB := b.Copy().Derive()
  // dA * b - a * dB
  o.operands[0] = &Operation{
    operator: Subtract,
    operands: OperationList{
      &Operation{
        operator: Multiply,
        operands: OperationList{dA, b},
      },
      &Operation{
        operator: Multiply,
        operands: OperationList{a, dB},
      },
    },
  }

  o.operands[1] = &Operation{
    operator: Power,
    operands: OperationList{ b, getConstant(2)},
  }
}

func getTemplateDerivedOperation(f OperatorType, operands OperationList) *Operation {
  g := operands[0]
  rv := &Operation{

  }
  switch f {
  case Sin: // sin(g) => cos(g)
    rv.operator = Cos
    rv.operands = OperationList{g}

  case Cos: // cos(g) => -sin(g)
    rv.operator = Multiply
    rv.operands = OperationList{
      getConstant(-1),
      &Operation{
        operator: Sin,
        operands: OperationList{g},
      },
    }

  case Tan: // => tan(g) => 1 / (cos(x))^2. Tests want (1 + tan(x)^2)
    /*
    rv.operator = Division
    rv.operands = OperationList{
      getConstant(1),
      &Operation{
        operator: Power,
        operands: OperationList{
          &Operation{
            operator: Cos,
            operands: OperationList{g},
          },
          getConstant(2),
        },
      },
    }
    */
    rv.operator = SumOp
    rv.operands = OperationList{
      getConstant(1),
      &Operation{
        operator: Power,
        operands: OperationList{
          &Operation{
            operator: Tan,
            operands: OperationList{g},
          },
          getConstant(2),
        },
      },
    }

  case ExpOp: // e(g) => e(g)
    rv.operator = ExpOp
    rv.operands = OperationList{g}

  case Ln: // ln(g) => 1 / g
    rv.operator = Division
    rv.operands = OperationList{
      getConstant(1),
      g,
    }

  case Power: // g^n => n * g
    rv.operator = Multiply
    rv.operands = OperationList{
      operands[1],
      &Operation{
        operator: Power,
        operands: OperationList{
          g,
          getConstant(operands[1].value - 1),
        },
      },
    }
  }

  return rv
}

// (f (g A)) => (* Derive(f GA) Derive((g A))
// f(g(x))' => f'(g(x)) * g'(x)
func functionDerive(op *Operation) {
  f := op.operator
  op.operator = Multiply
  op.operands = OperationList{
    op.operands[0].Copy().Derive(),
    getTemplateDerivedOperation(f, op.operands),
  }
}

// constant' = 0
func constantDerive(op *Operation) {
  op.value = 0
}

func varDerive(op *Operation) {
  op.operator = Constant
  op.value = 1
}

func (op OperatorType) DeriveMethod() func(o *Operation) {
  switch op {
  case SumOp:
    return sumDerive
  case Subtract:
    return sumDerive
  case Multiply:
    return multiplyDerive
  case Division:
    return divideDerive
  case Sin:
    return functionDerive
  case Cos:
    return functionDerive
  case Tan:
    return functionDerive
  case Ln:
    return functionDerive
  case ExpOp:
    return functionDerive
  case Power:
    return functionDerive
  case Constant:
    return constantDerive
  case VarOp:
    return varDerive
  }
  return nil
}

func getOpType(op string) OperatorType {
  for i, v := range opStrings {
    if v == op {
      return OperatorType(i)
    }
  }
  return Constant
}

// This would be so much simpler if Go supported recursive groups...
var isOp = regexp.MustCompile(`\s*\(`)
var parseOp = regexp.MustCompile(`^\s*\(\s*([^\s]+)\s+(.+?)\s*\)\s*$`)
var singleWord = regexp.MustCompile(`^\s*([^\s]+)\s*$`)

func findMatchingParenIndex(op string) int {
  numOpen := 0
  incr := 0
  for i := 0; i < len(op); i++ {
    switch op[i] {
    case ')':
      numOpen--
    case '(':
      numOpen++
      incr = 1
    }
    if numOpen == 0 {
      return i + incr
    }
  }
  return -1
}

// op is either one or two operands, already trimmed. Need this cause Go doesn't have recursive groups
func parseOperands(op string) ([]string, error) {
  endIndex := findMatchingParenIndex(op)
  if endIndex == -1 { // Unbalanced parens
    return nil, fmt.Errorf("unbalanced parens at %s", op)
  }
  if endIndex == 0 { // First operand isn't an expression
    endIndex = strings.Index(op, " ")
  }
  if endIndex < 0 {
    endIndex = len(op)
  }
  return []string{op[:endIndex], strings.TrimLeft(op[endIndex:], " "), }, nil
}

// Parses an operation of type (op P1 P2) or (op P1)
func parsePrefixOp(op string) (*Operation, error) {
  if op == "" {
    return nil, nil
  }
  if isOp.MatchString(op) {

    if !parseOp.MatchString(op) {
      return nil, fmt.Errorf("cannot parse %s", op)
    }
    elements := parseOp.FindStringSubmatch(op)
    opType := getOpType(elements[1])
    operands, err := parseOperands(elements[2])
    if err != nil {
      return nil, err
    }
    if len(operands[1]) == 0 {
      operands = operands[:1]
    }
    if wantedArgs := opType.OperandNumber(); wantedArgs != len(operands) {
      return nil,  fmt.Errorf("invalid number of operands for %v(%d - %d) => %+v)",
        opType, wantedArgs, len(operands), operands)
    }

    rv := &Operation{
      operator: opType,
      operands: make([]*Operation, len(operands)),
    }
    for i, operandText := range operands {
      if operation, err := parsePrefixOp(operandText); err != nil {
        return nil, err
      } else {
        rv.operands[i] = operation
      }
    }
    return rv, nil

  } else if !singleWord.MatchString(op){
    return nil, fmt.Errorf("expected a single operand: %s", op)
  }

  term := singleWord.FindStringSubmatch(op)[1]
  rv := &Operation{
    operator: getOpType(term),
    operands: nil,
  }
  if rv.operator == Constant {
    if value, err := strconv.ParseFloat(term, 10); err != nil {
      return nil, fmt.Errorf("%s is an invalid constant: %+v", term, err)
    } else {
      rv.value = value
    }
  }
  return rv, nil

}

func Diff(f string) string {
  parsedOp, err := parsePrefixOp(f)
  if err != nil {
    return fmt.Sprintf("Error: %+v", err)
  }
  return parsedOp.Derive().Simplify().String()
}
