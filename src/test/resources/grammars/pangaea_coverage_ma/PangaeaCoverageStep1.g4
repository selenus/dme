grammar PangaeaCoverage;

substruct       :   subelem+;

subelem         :       key ': ' value '*';
key                     :       ID;
value           :       ID;

WS      :       [ \t\r\n]+ -> skip;
ID      :       ~(':'|'*')+;