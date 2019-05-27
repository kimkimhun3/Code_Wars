def break_pieces(string)
  complex_shape = Shape.new(string)
  complex_shape.component_shapes.map(&:to_s)
end

class UnboundedRegion < StandardError; end

class Shape
  attr_accessor :string
  attr_accessor :source, :dest # used to extract and build component shapes
  
  def initialize(string)
    @string = remove_leading_lines_and_spaces(string)
  end
  
  def to_s
    string
  end
  
  def component_shapes
    self.source = to_matrix(string)
    
    [].tap do |shapes|
      loop do
        begin
          shape = extract_one_shape || break
        rescue UnboundedRegion => e
          next
        end
        string = join_matrix(outline_spaces(shape))
        shapes << self.class.new(string)
      end
    end
  end
  
  def to_matrix(string)
    string.lines.map do |line|
      indent = line.index(/\S/) || 0
      line.chomp.chars[indent..-1]
    end
  end
  
  def join_matrix(matrix)
    matrix.map do |row|
      row ||= []
      indent = row.index { |item| item } || 0
      ' ' * indent + row[indent..-1].join
    end.join("\n")
  end
  
  def remove_leading_lines_and_spaces(string)
    indent = string.scan(/^\s*/).map(&:size).min
    string.lines
          .select { |line| line =~ /\S/ }
          .map { |line| line[indent..-1] }
          .join
  end
    
  def extract_one_shape
    self.dest = []
    coords = first_space
    return nil unless coords
    fill_spaces(*coords)
    dest
  end
  
  def first_space
    source.each_with_index do |row, y|
      if (x = row.index(' '))
        return [x, y]
      end
    end
    
    nil
  end

  # copies contiguous spaces from source to dest, changing source cells to nil
  def fill_spaces(x, y)
    source[y] ||= []
    value = source[y][x]
    source[y][x] = nil # nil out to advance search and reduce recursion

    if value == ' ' 
      if x < 1 || y < 1 || x > source[y].size - 2 || y > source.size - 2
        raise UnboundedRegion
      end
  
      (self.dest[y] ||= [])[x] = ' ' # copy space

      # recursively fill from adjacent spaces
      fill_spaces(x + 1, y)
      fill_spaces(x - 1, y)
      fill_spaces(x, y - 1)
      fill_spaces(x, y + 1)
    end
  end
  
  def outline_spaces(matrix)
    matrix.each_with_index do |row, y|
      (row || []).each_with_index do |value, x|
        if value == ' '
          h = horizontal_bar_rows(x, y, matrix)
          v = vertical_bar_columns(x, y, matrix)
          
          h.each do |j|
            inside_corner = space?(x - 1, j, matrix) || space?(x + 1, j, matrix)
            (matrix[j] ||= [])[x] = inside_corner ? '+' : '-'
          end
          
          v.each do |i|
            inside_corner = space?(i, y - 1, matrix) || space?(i, y + 1, matrix)
            (matrix[y] ||= [])[i] = inside_corner ? '+' : '|'
            
            # outside corners
            h.each { |j| (matrix[j] ||= [])[i] = '+' unless space?(i, j, matrix) }
          end
        end
      end
    end
  end
  
  def horizontal_bar_rows(x, y, matrix)
    [y - 1, y + 1].reject { |row| space?(x, row, matrix) }
  end
  
  def vertical_bar_columns(x, y, matrix)
    [x - 1, x + 1].reject { |col| space?(col, y, matrix) }
  end
      
  def space?(x, y, matrix)
    return nil if x < 0 || y < 0
    (matrix[y] || [])[x] == ' '
  end
end
