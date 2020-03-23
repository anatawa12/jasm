grammar Parser;

@header {
package com.anatawa12.jasm;
}

jasm_file
  : jasm_header
    class_header
    class_element*
  ;

jasm_header
  : jasm_header_element*
  ;

jasm_header_element
  : '.autoline'
  ;

class_header
  : bytecode_directive?
    source_directive?
    class_directive
    super_directive?
    implements_directive*
    signature_directive?
    enclosing_directive?
    debug_directive?
    inner_class_directive*
  ;

bytecode_directive
  : '.bytecode' integer'.'integer
  ;

source_directive
  : '.source' string
  ;
class_directive
  : '.class' access_flags internal_name
  ;

super_directive
  : '.super' internal_name
  ;

implements_directive
  : '.implements' internal_name
  ;

signature_directive
  : '.signature' string
  ;

enclosing_directive
  : '.enclosing' 'method' internal_name ID method_descriptor
  | '.enclosing' 'class' internal_name
  ;

debug_directive
  : '.debug' string
  ;

inner_class_directive
  : '.inner' access_flags internal_name ('inner' ID)? ('outer' internal_name)?
  ;


class_element
  : method_block
  | field_block
  | '.deprecated'
  | member_annotation_block
  ;

method_block
  : '.method' access_flags ID method_descriptor
    method_statement*
    '.end' 'method'
  ;

method_statement
  : '.limit' 'stack' integer
  | '.limit' 'locals' integer
  | '.line' integer
  | '.var' integer 'is' ID field_descriptor ('signature' field_signature)? 'from' label 'to' label
  | '.throws' internal_name
  | '.catch' internal_name 'from' label 'to' label 'using' label
  | '.signature' method_signature
  | '.deprecated'
  | stack_block
  | instruction
  | label_definition
  | annotation_block
  ;

stack_block
  : '.stack' ('use' 'locals')
    locals_*
    stacks*
    '.end' 'stack'
  ;

locals_
  : 'locals' stackmap_type
  ;

stacks
  : 'stacks' stackmap_type
  ;

stackmap_type
  : 'Top'
  | 'Integer'
  | 'Float'
  | 'Long'
  | 'Double'
  | 'Null'
  | 'UninitializedThis'
  | 'Object' internal_name
  | 'Uninitialized' label
  ;

instruction: 'insn' ;//TODO
label: ID;
label_definition: ID':';

field_block
  : '.field' access_flags ID field_descriptor ('=' value)?
  | '.field' access_flags ID field_descriptor ('=' value)?
     field_attribute*
    '.end' 'field'
  ;

field_attribute
  : '.deprecated'
  | '.signature' field_signature
  | annotation_block
  ;

member_annotation_block
  : '.annotation' annotation_type
    (ID annotation_pair)*
    '.end' 'annotation'
  | '.annotation' 'default'
    annotation_pair
    '.end' 'annotation'
  ;

annotation_block
  : '.annotation'
    (ID annotation_pair)*
    '.end' 'annotation'
  ;

annotation_array_block
  : '.annotation' 'array'
    annotation_pair*
    '.end' 'annotation'
  ;

annotation_type
  : 'visible'
  | 'invisible'
  | 'visiblepararm' integer
  | 'invisiblepararm' integer
  ;

annotation_pair
  : 'B' '=' integer
  | 'C' '=' integer
  | 'D' '=' double
  | 'F' '=' float
  | 'I' '=' integer
  | 'J' '=' long
  | 'S' '=' integer
  | 'Z' '=' boolean
  | 's' '=' string
  | 'e' '=' annotation_enum
  | 'c' '=' field_descriptor
  | '@' '=' annotation_block
  | '[' '=' annotation_array_block
  ;

annotation_enum: internal_name internal_name;

method_descriptor:; //TODO
field_descriptor:; // TODO
method_signature: string;
field_signature: string;

value
  : integer
  | double
  | string
  | float
  | long
  ;

double:;//TODO
integer:;//TODO
string:;//TODO
boolean: 'true' | 'false' ;
float: double'f' | integer'f';
long: integer'L';

access_flags
  : access_flag*
  ;

access_flag
  : 'public'
  | 'static'
  ;

internal_name
  : ID ('/' ID)
  ;
