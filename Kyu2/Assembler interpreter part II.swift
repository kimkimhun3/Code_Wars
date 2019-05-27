precedencegroup ForwardApplication {
    associativity: left
    higherThan: AssignmentPrecedence
}
precedencegroup EffectfulComposition {
    associativity: left
    higherThan: ForwardApplication
}
precedencegroup ForwardComposition {
    associativity: left
    higherThan: ForwardApplication, EffectfulComposition
}

infix operator |>: ForwardApplication
infix operator >>>: ForwardComposition
infix operator >=>: EffectfulComposition


func |> <A, B>(a: A, f: (A) -> B) -> B { return f(a) }
func >>> <A, B, C>(f: @escaping (A) -> B, g: @escaping (B) -> C) -> ((A) -> C) {
    return { a in g(f(a)) }
}
func >=> <A, B, C>(
    _ f: @escaping (A) -> (B, [String]),
    _ g: @escaping (B) -> (C, [String])
    ) -> (A) -> (C,[String]) {
    return { a in
        let (b,logs) = f(a)
        let (c,moreLogs) = g(b)
        return (c, logs + moreLogs)
    }
}


typealias Reg = Character
typealias Registers = [Reg: Int]

enum Value {
    case register(Reg)
    case immediate(Int)
    case string(String)
}
enum ExecError: Error {
    case unexpectedEnd
}

enum OpCode {
    case mov(Reg,Value)
    case inc(Reg)
    case dec(Reg)
    case add(Reg,Value)
    case sub(Reg,Value)
    case mul(Reg,Value)
    case div(Reg,Value)
    case label(String)
    case jmp(String)
    case cmp(Value, Value)
    case jne(String)
    case je(String)
    case jge(String)
    case jg(String)
    case jle(String)
    case jl(String)
    case call(String)
    case ret
    case msg([Value])
    case end
    case nop
}


func stripComments(_ line: Substring) -> Substring {
    return line.prefix(while: {$0 != ";"})
}
func trim(_ line: Substring) -> Substring {
    return line.drop(while: {$0 == " " || $0 == "\t"})
}
func take(from ss: Substring, until ch: Character) -> (Substring, Substring) {
    let idx = ss.index(of: ch) ?? ss.endIndex
    let front = ss.prefix(upTo: idx), back = ss.suffix(from: idx).dropFirst() |> trim
    return (front,back)
}
func parse(instruction: Substring) -> OpCode {
    guard !instruction.isEmpty else { return .nop }
    let getRV = { (ss: Substring) -> (Reg,Value) in
        let (reg,back) = take(from: ss, until: ","),
            (source,_) = take(from: back, until: " ")
        var val: Value

        guard let register = reg.first else { fatalError("Invalid instruction: \(instruction)") }
        if let i = Int(source) {
            val = .immediate(i)
        } else {
            guard let sourceRegister = source.first else { fatalError("Invalid instruction: \(instruction)") }
            val = .register(sourceRegister)
        }
        return (register,val)
    }
    
//    print("\t++ instruction: -\(instruction)-")
    var (code, rest) = take(from: instruction, until: " ")
    
//    print("\t-- code: \"\(code)\", rest: \"\(rest)\"")

    switch code {
    case "mov":
        let (reg,val) = getRV(rest)
        return .mov(reg, val)
    case "inc":
        let (target,_) = take(from: rest, until: " ")
        guard let reg = target.first else { fatalError("Invalid instruction: \(instruction)") }
        return .inc(reg)
    case "dec":
        let (target,_) = take(from: rest, until: " ")
        guard let reg = target.first else { fatalError("Invalid instruction: \(instruction)") }
        return .dec(reg)
    case "add":
        let (reg,val) = getRV(rest)
        return .add(reg, val)
    case "sub":
        let (reg,val) = getRV(rest)
        return .sub(reg, val)
    case "mul":
        let (reg,val) = getRV(rest)
        return .mul(reg, val)
    case "div":
        let (reg,val) = getRV(rest)
        return .div(reg, val)
    case "jmp":
        let (target,_) = take(from: rest, until: " ")
        return .jmp(String(target))
    case "cmp":
        let (xV,back) = take(from: rest, until: ","),
            (yV,_) = take(from: back, until: " ")
        var valX: Value, valY: Value
        
        if let x = Int(xV) {
            valX = .immediate(x)
        } else {
            guard let xReg = xV.first else { fatalError("Invalid instruction: \(instruction)") }
            valX = .register(xReg)
        }
        if let y = Int(yV) {
            valY = .immediate(y)
        } else {
            guard let yReg = yV.first else { fatalError("Invalid instruction: \(instruction)") }
            valY = .register(yReg)
        }
        return .cmp(valX, valY)
    case "jne":
        let (target,_) = take(from: rest, until: " ")
        return .jne(String(target))
    case "je":
        let (target,_) = take(from: rest, until: " ")
        return .je(String(target))
    case "jge":
        let (target,_) = take(from: rest, until: " ")
        return .jge(String(target))
    case "jg":
        let (target,_) = take(from: rest, until: " ")
        return .jg(String(target))
    case "jle":
        let (target,_) = take(from: rest, until: " ")
        return .jle(String(target))
    case "jl":
        let (target,_) = take(from: rest, until: " ")
        return .jl(String(target))
    case "call":
        let (target,_) = take(from: rest, until: " ")
        return .call(String(target))
    case "ret": return .ret
    case "msg":
        var vals = [Value]()
        while !rest.isEmpty {
            if rest.first! == "'" {
                let (front, back) = take(from: rest.dropFirst(), until: "'")
                vals.append(.string(String(front)))
                rest = take(from: back, until: ",").1
            } else {
                let (front,back) = take(from: rest, until: ",")
                guard let r = front.first else { fatalError("Invalid instruction: \(instruction)") }
                vals.append(.register(r))
                rest = back
            }
        }
        return .msg(vals)
    case "end": return .end
    default: // label
        let (label,_) = take(from: code, until: ":")
        return .label(String(label))
    }
}

func assemblerInterpreter(_ program: String) throws -> String {
    var registers = Registers(), ip = 0, pSlice = trim(program[...]), lprog = [OpCode](),
    prog = [OpCode](), stack = [Int](), labels = [String: Int](), buffer = [[Value]](), cmpFlag: Int? = nil
    let resolveIntValue = { (val: Value) -> Int in
        var v: Int
        switch val {
        case .immediate(let i): v = i
        case .register(let r):
            guard let i = registers[r] else { fatalError("Read from uninitialized register: \(r)") }
            v = i
        case _: fatalError("This should never happen: .string value in immediate or register place")
        }
        return v
    }
    let execInstruction = { (ins: OpCode) -> String? in
        var nextIP = ip + 1
        switch ins {
        case .mov(let reg, let val):
            registers[reg] = resolveIntValue(val)
        case .inc(let reg):
            guard let r = registers[reg] else { fatalError("Read from uninitialized register: \(reg)") }
            registers[reg] = r+1
        case .dec(let reg):
            guard let r = registers[reg] else { fatalError("Read from uninitialized register: \(reg)") }
            registers[reg] = r-1
        case .add(let reg, let val):
            guard let r = registers[reg] else { fatalError("Read from uninitialized register: \(reg)") }
            registers[reg] = r + resolveIntValue(val)
        case .sub(let reg, let val):
            guard let r = registers[reg] else { fatalError("Read from uninitialized register: \(reg)") }
            registers[reg] = r - resolveIntValue(val)
        case .mul(let reg, let val):
            guard let r = registers[reg] else { fatalError("Read from uninitialized register: \(reg)") }
            registers[reg] = r * resolveIntValue(val)
        case .div(let reg, let val):
            guard let r = registers[reg] else { fatalError("Read from uninitialized register: \(reg)") }
            registers[reg] = r / resolveIntValue(val)
        case .jmp(let label):
            guard let addr = labels[label] else { fatalError("Jump to unknown label: \(label)") }
            nextIP = addr
        case .cmp(let valX, let valY):
            let x = resolveIntValue(valX), y = resolveIntValue(valY)
            cmpFlag = x == y ? 0 : x < y ? -1 : 1
        case .jne(let label):
            guard let flag = cmpFlag else { fatalError("jne instruction without preceeding cmp") }
            if flag != 0 {
                guard let addr = labels[label] else { fatalError("Jump to unknown label: \(label)") }
                nextIP = addr
                cmpFlag = nil
            }
        case .je(let label):
            guard let flag = cmpFlag else { fatalError("je instruction without preceeding cmp") }
            if flag == 0 {
                guard let addr = labels[label] else { fatalError("Jump to unknown label: \(label)") }
                nextIP = addr
                cmpFlag = nil
            }
        case .jge(let label):
            guard let flag = cmpFlag else { fatalError("jne instruction without preceeding cmp") }
            if flag != -1 {
                guard let addr = labels[label] else { fatalError("Jump to unknown label: \(label)") }
                nextIP = addr
                cmpFlag = nil
            }
        case .jg(let label):
            guard let flag = cmpFlag else { fatalError("jne instruction without preceeding cmp") }
            if flag == 1 {
                guard let addr = labels[label] else { fatalError("Jump to unknown label: \(label)") }
                nextIP = addr
                cmpFlag = nil
            }
        case .jle(let label):
            guard let flag = cmpFlag else { fatalError("jne instruction without preceeding cmp") }
            if flag != 1 {
                guard let addr = labels[label] else { fatalError("Jump to unknown label: \(label)") }
                nextIP = addr
                cmpFlag = nil
            }
        case .jl(let label):
            guard let flag = cmpFlag else { fatalError("jne instruction without preceeding cmp") }
            if flag == -1 {
                guard let addr = labels[label] else { fatalError("Jump to unknown label: \(label)") }
                nextIP = addr
                cmpFlag = nil
            }
        case .call(let label):
            guard let addr = labels[label] else { fatalError("Jump to unknown label: \(label)") }
            stack.append(nextIP)
            nextIP = addr
        case .ret:
            guard let addr = stack.popLast() else { fatalError("ret with no corresponding call") }
            nextIP = addr
        case .msg(let vals):
            buffer.append(vals)
        case .end:
            return buffer.map { vals in
                vals.map { (val: Value) -> String in
                    switch val {
                    case .string(let msg):
                        return msg
                    case .register(let reg):
                        guard let r = registers[reg] else { fatalError("Read from uninitialized register: \(reg)") }
                        return "\(r)"
                    case _: fatalError("This should never happen. .immediate value in a message")
                    }
                }.joined()
            }.joined(separator: "\n")
        case .nop, .label(_): let _ = nextIP
        }
        ip = nextIP
        return nil
    }
    
    while !pSlice.isEmpty {
        let instIdx = pSlice.index(of: "\n") ?? pSlice.endIndex
        let instruction =
                pSlice.prefix(upTo: instIdx)
            |> trim
            |> stripComments
        pSlice = pSlice.suffix(from: instIdx != pSlice.endIndex ? pSlice.index(after: instIdx) : instIdx)
        let op = parse(instruction: instruction)
        lprog.append(op)
    }
    // remove labels
    var idx = 0
    for op in lprog {
        if case let .label(label) = op {
            labels[label] = idx
        } else {
            idx += 1
            prog.append(op)
        }
    }
    
    while 0..<prog.count ~= ip {
        if let retval = execInstruction(prog[ip]) { return retval }
    }
    throw ExecError.unexpectedEnd
}
