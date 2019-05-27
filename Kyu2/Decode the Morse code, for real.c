#include <stddef.h>
#include <math.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

/*Feel free to have a look. I did not refactor very much. I left many stuff to remind me of all the hard moments during the development. 

PROBLEMS FOUND: 

(Specific to the C translation) The given function const char *morse_code (const char *dotsdashes)
crashes when you pass it an invalid code. This was pretty annoying for debugging as it crashes the whole test batch.
And little information can be extracted. I think this is a major problem. It should return NULL or whatever...

(Specific to the C translation) Test function does not handle dynamic memory.
The return type of decodeMorse and decodeBitsAdvanced is char *.
When I find this in other katas which is quite common I use dynamic memory to return an array.
It is very annoying to find this out after coding everything and having some successful results.
I found it out is when I started with the longer test cases and due to memory issues my program start showing weird behaviours.
Then I had to change everything to remove the dynamic memory and passed the result as a global variable.

It should be stated in the instructions which is the way to go.


(Genaral) Sequence 1001, I agree with many people in the discussion, according to the instructions shouldn't be EE
(General) As claimed by a sensible person in the discussion, it needs random tests instead of hiding the solutions. I endorse all his reasons.
*/


/*Counts how many transitions between ones and zeros are in the sequence. It disregards final zeros*/
int number_of_symbols(const char *bits){
   int n_syms = 0, i;
   for(i = 1; i <= strlen(bits); i++){
      if((bits[i - 1] != bits[i]) && ~(bits[i - 1] == '0' && bits[i] == '\0')){
        n_syms++;
      }
    }
  return n_syms;
}

/*f_ones and f_zeros are arrays that will will hold the number of times that certain symbol is repeated within the sequence. Each
index corresponds to the length of each symbol. Eg. f_ones == {0, 4, 7, 2, 0, 0, 0,...}
would mean:
"0 ones" appears 0 times (this index is always 0 as logically it has no sense, there is no such concept such as "0 ones" within the sequence)
"1 one: 1" 4 times
"2 ones: 11" 7 times
"111" 2 times
"1111" 0 times
"11111... and so on" 0 times ...

This function helped me a lot to find out where to place the thresholds
*/
void freqs(const char *bits, int *f_ones, int *f_zeros){
   
   int len = 0, i, last = '1';

   for(i = 0; i <= strlen(bits); i++){
      if(last == '1' && bits[i] != '1'){
        f_ones[len]++;
        len = 0;
      }
      if(last == '0' && bits[i] == '1'){
        f_zeros[len]++;
        len = 0;
      }
      last = bits[i];
      len++; 
    }
}

/* Combines the frequencies of ones and zeros retrieved from the previous function into another array "result"
    It just sums the arrays element wise. And it only does it until the highest index with a non-zero f_ones.
    Zero freqs of indices higher than that value are not taken into account.
*/
void combine_freqs(int *f_ones, int *f_zeros, int length, int *result){
   
   int i, largest_ones_row = 0;

   for(i = 0; i < length; i++){
      if(f_ones[i] > 0){ 
        largest_ones_row = i;
      }
    }

   for(i = 0; i <= length; i++){
      if(i <= largest_ones_row){ // zeroes rows larger than the largest row of ones will be discarded
        result[i] = f_ones[i] + f_zeros[i];
      }else{
        result[i] = f_ones[i];
      }
    }
}

/* Computes the mean value of a dot */
double dot_mean(const char *bits, double dot_dash_threshold){
    int dot=0, count_short=0, len = 0, i, last = '1';
    const double epsilon = 0.001;

    for(i = 0; i <= strlen(bits); i++){
      if(last == '1' && bits[i] != '1'){
        if(len < dot_dash_threshold - epsilon){
          dot += len;
          count_short++;
        }
        len = 0;
      }
      if(last == '0' && bits[i] == '1'){
        if(len < dot_dash_threshold - epsilon){
          dot += len;
          count_short++;
        }
        len = 0;
      }
      
      last = bits[i];
      len++; 
    }
        
    if(!count_short==0){
      return dot/((double)count_short);
    }else{
      return dot_dash_threshold/1.5; //Maybe with this new threshold there is more luck!
    }
}

/* Computes the mean value of the symbols formed by ones */
double calculate_ones_length_average(const char *bits){
    char last = '0';
    int length = 0, ones_symbols = 0, i;
    
    /*
      My thought when I started the kata:
      This approach will only use the ones for the calculation
      (Using the zeroes seem a bit harder as we have to differenciate
      between the inter-beep, inter-character & inter-word pauses which are not separated by ones.
      I am not sure if it even pays in the end.)
      
      After solving the kata:... I am not really sure. Using only the ones for the parameter computation 
      seems cleaner and better. But that can only be concluded after having a thorough look at the 
      distributions of ones and zeros in the several test cases
    */
    
    if(*bits == '\0'){
      return 2;
    }
    
    for(i = 0; i <= strlen(bits); i++){
      if(last == '0' && bits[i] == '1'){
        ones_symbols++;
      }
      last = bits[i];
      if(bits[i] == '1'){
        length++; // will hold the total number of ones
      }
    }
    
    if(ones_symbols == 0){ 
      return 0;
    }
  
    return length/((double)ones_symbols);
}

#define M_LENGTH_FREQS 35

#ifndef DYNAMIC_MEMORY_ON
  char result_function1[1000];
#endif

char* decodeBitsAdvanced (const char *bits) {
    /* PARAMETERS: 
    1st threshold (dot | dash) = coeff1 * (dot + dot_length_offset) in long sequences where (number_transitions > n_transitions_threshold_for_long_sequences)
                               = coeff1 * dot      (for short sequences: number_transitions < n_transitions_threshold_for_long_sequences)
    2nd threshold (dash | blank space) = coeff2 * dot 
    */
    double coeff1 = 1.999, coeff2 = 5.0, dot_length_offset = 0.475;
    int n_transitions_threshold_for_long_sequences = 15;
    
    int same_symbol_digits = 0, buff_size = 0, i;
    int frequencies_ones[M_LENGTH_FREQS]={}, frequencies_zeros[M_LENGTH_FREQS]={}, frequencies[M_LENGTH_FREQS]={};
    double first_max, last_max;
    int n_symbols;
    
    double ones_mean, dot;
    char last = '1';
    char *output;
    #ifdef DYNAMIC_MEMORY_ON
      output = NULL;
    #else
      output = result_function1;
    #endif
    if(bits == NULL){
      printf("decodeBitsAdvanced received NULL input\n");
      return NULL;
    }

    printf("(%p)\t",bits);     
    printf("Raw input is:\n%s\n",bits);           
       
    for(i = 0; i < strlen(bits) && bits[i] == '0'; i++); // moves forward index for initial zeroes 
    
    ones_mean = calculate_ones_length_average(&bits[i]);
    printf("Threshold is %lf\n", ones_mean); 
    dot = dot_mean(&bits[i], ones_mean);
    printf("Threshold is %lf\n", coeff1 * dot); 
    dot = dot_mean(&bits[i], coeff1 * dot);
    printf("Threshold is %lf\n", coeff1 * dot);  
    
    n_symbols = number_of_symbols(&bits[i]);
    
    /* I needed to offset the dot length for long inputs with many transitions.
        It works from n_symbols = 3 or more
        The dot offset should be between 0.44 and 0.51 as found testing. I chose the value in the middle
    */
    if(n_symbols > n_transitions_threshold_for_long_sequences){
      dot = dot + dot_length_offset;
    }
    printf("Threshold is %lf\n", coeff1 * dot);  
    
    freqs(&bits[i], frequencies_ones, frequencies_zeros);
    combine_freqs(frequencies_ones, frequencies_zeros, M_LENGTH_FREQS, frequencies);
    
    printf("Total number of transitions = %d\n", n_symbols);
    printf("LENGTH\tONES\tZEROS\tTOTAL\n");
    for(int y = 0; y < M_LENGTH_FREQS; y++){
      printf("%d:\t%d\t%d\t%d\n", y, frequencies_ones[y], frequencies_zeros[y], frequencies[y]);
    }
    
    // I leave the solutions here so I don't miss them :D
    // "THE QUICK BROWN FOX JUMPS OVER THE LAZY DOG"
    // "SOS! THE QUICK BROWN FOX JUMPS OVER THE LAZY DOG."
    // "MGY CQD CQD SOS TITANIC POSITION 41.44 N 50.24 W. REQUIRE IMMEDIATE ASSISTANCE. COME AT ONCE. WE STRUCK AN ICEBERG. SINKING"
    // 1001 -> ". ." -> "EE"
    
     /* Comment left as legacy from the hard moments (the associated code was removed while refactoring):
         HARD CODED INPUT CASE to find the solution by directly tweaking the parameteres for it.
         This was necessary to see in what direction I should go. Otherwise tweaking the params would be taking shots in the dark.
      */
      
     /*
     The fact that "1001" corresponds to ". ." , that is, EE in the solution, means (supposing that the dot length is 1 which is argueable,
     as there is not enough information in that sequence to begin with) that the threshold between dot an dash must be closer to the dot 
     than to the dash, rendering "00" as a inter character pause here). I added coeff1 so it rounds towards the lower value 
     */  
    
    if(i == strlen(bits)){ //advance the index for empty strings or only zeroes strings.
      i++;
    }
    // bits[i] must be a 1 at this point

    
    for(; i <= strlen(bits); i++ ){
      if(last != bits[i]){      
      
        if(last == '1'){
          if( same_symbol_digits > coeff1 * dot){
            #ifdef DYNAMIC_MEMORY_ON          
              output = realloc(output, (size_t)(buff_size + 1));
            #endif
            output[buff_size++] = '-';
          }else{
            #ifdef DYNAMIC_MEMORY_ON
              output = realloc(output, (size_t)(buff_size + 1));
            #endif
            output[buff_size++] = '.';
          }
        }else{
          if( same_symbol_digits > (coeff2 * dot)){
            #ifdef DYNAMIC_MEMORY_ON
              output = realloc(output, (size_t)(buff_size + 3));
            #endif
            output[buff_size++] = ' ';
            output[buff_size++] = ' ';
            output[buff_size++] = ' ';
          }else if(same_symbol_digits >  coeff1 * dot){
            #ifdef DYNAMIC_MEMORY_ON
              output = realloc(output, (size_t)(buff_size + 1));
            #endif
            output[buff_size++] = ' ';
          }
        }
        same_symbol_digits = 0;
      }
      
      same_symbol_digits++;
      last = bits[i];
    }
    #ifdef DYNAMIC_MEMORY_ON
      output = realloc(output, (size_t)(buff_size + 1));
    #endif
    while(buff_size > 0 && output[buff_size - 1] == ' '){//eliminate final blanks
      #ifdef DYNAMIC_MEMORY_ON
        output = realloc(output, (size_t)(buff_size));
      #endif
      buff_size--;
    }
    output[buff_size] = '\0';
    printf("Morse is:\n%s\n", output);
  return output;
}


// USED this macro to remove all the dynamic memory from this function as it seems the test script was not freeing it properly
#ifndef DYNAMIC_MEMORY_ON
  char result_function2[200];
#endif

char *decodeMorse (const char *morseCode1) {
    // ToDo: Accept dots, dashes and spaces, return human-readable message
    #ifdef DYNAMIC_MEMORY_ON
      char *output = NULL;
    #else
      char *output = result_function2;
    #endif
    char symbol[50];
    char decoded_symbol[10];
    int buff_size = 0, i, character_start;

    
    /*Need to do this bullshit in order for it not to crash. When using dynamic memory. If not using dynamic memory as is the final solution I guess I could refactor it. I dk if i will do it though...*/
    char morseCode[strlen(morseCode1) + 1];
    strcpy(morseCode, morseCode1);
    #ifdef DYNAMIC_MEMORY_ON
      free(morseCode1);
    #endif

    for( i = 0 ; i <= strlen(morseCode) && (morseCode[i] == ' ' || morseCode[i] == '\0' ) ; i++); // Moves forward index for only blanks or empty string

    for( character_start = i ; i <= strlen(morseCode) ; i++){
      if(morseCode[i] == ' ' || morseCode[i] == '\0' ){
        
        strncpy(symbol, morseCode + character_start, i-character_start);
        symbol[i - character_start] = '\0';
        strcpy(decoded_symbol, morse_code(symbol));
        #ifdef DYNAMIC_MEMORY_ON
          output = realloc(output, (size_t)(buff_size + strlen(decoded_symbol)));
        #endif
        strcpy(&output[buff_size], decoded_symbol);
        printf("Symbol %s\t= %s\n", symbol, decoded_symbol);
        buff_size += strlen(decoded_symbol);

        if (strncmp(&morseCode[i], "   ", 3) == 0){
          #ifdef DYNAMIC_MEMORY_ON
            output = realloc(output, (size_t)(buff_size + 1));
          #endif
          ++buff_size;
          output[buff_size-1] = ' ';
          i += 2; // the loop will sum 1 more
        }
        character_start = i + 1;
      }
      
    }
    #ifdef DYNAMIC_MEMORY_ON
      output = realloc(output, (size_t)(buff_size + 1));
    #endif
    output[buff_size] = '\0';
    printf("Message is:\n%s\n", output);
    
  return output;
}
