grammar PangaeaCoverage;

substruct       :   subelem+;

subelem         :       key ': ' value '*'
                        |       key ': ' value;
                       
key                     :       ID;
value           :       date
                        |       ID;
                       
date            :       ID ':' ID ':' ID;


WS      :       [ \t\r\n]+ -> skip;
ID      :       ~(':'|'*')+;