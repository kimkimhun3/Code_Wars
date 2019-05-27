DEBUG = false
def find_position(str)
  puts "str=#{str}".red if DEBUG
  # find first number that starts the sequence
  if str[/^0+$/]
    number = ("1" + str).to_i
    offset = 1
  else
    number, offset = (1..str.length).find do |digits| # search by possible length of number
      puts "digits=#{digits}".blue if DEBUG
      res = (0..digits - 1).map do |off| # offset: the leading number might be cut off -- check numbers
        head = str[digits - off, off] # assemble number using offset
        tail = str[0, digits - off]
        n = (head + tail).to_i
        next if n == 0
        option = n.step.take(str.length / digits + off + 1).map(&:to_s).join # make comparing sequence
        puts "offstr=#{head + tail}, n=#{n}, off=#{off}, opt=#{option[off, str.length]}" if DEBUG
        option[off, str.length] == str ? [n, off] : nil
      end.compact
      if (match = str[0, digits].match(/^(9+)([1-9]\d*)$/))
        nines, trailing = match.captures
        trailing = (trailing.to_i - 1).to_s
        n = (trailing + nines).to_i
        off = trailing.length
        option = n.step.take(str.length / digits + off + 1).map(&:to_s).join # make comparing sequence
        puts "9 h/t=#{trailing}/#{nines}, n=#{n}, off=#{off}, opt=#{option[off, str.length]}" if DEBUG
        res << [n, off] if option[off, str.length] == str
      end
      p res if DEBUG
      break res.min unless res.empty?
    end || [str.to_i, 0] # number is standalone
    unless str.start_with? "0"
      number, offset = [[number, offset], [str.to_i, 0]].min
    end
  end
  # calculate position of number in infinite sequence
  puts "=> #{number}".green if DEBUG
  number_digits = number.to_s.length
  (1..number_digits - 1).map {|i| i * 9 * 10 ** (i - 1)}.sum +
      number_digits * (number - 10 ** (number_digits - 1)) + offset
end
