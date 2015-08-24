grammar PangaeaCoverage;

substruct       :   subelem+;

subelem         :       (longitude | latitude | start | end | minDepth | maxDepth | otherElem) SEPARATOR?;


longitude       :       'LONGITUDE' ': ' value;                
latitude        :       'LATITUDE' ': ' value;
start           :       'DATE/TIME START' ': ' value;
end                     :       'DATE/TIME END' ': ' value;
minDepth        :       'MINIMUM DEPTH, sediment/rock' ': ' value;
maxDepth        :       'MAXIMUM DEPTH, sediment/rock' ': ' value;

otherElem       :       key     ': ' value;

key                     :       ID;
value           :       DATE
                        |       ID;

ID      :       ~(' '|':'|'*') ~(':'|'*')+ ~(' '|':'|'*');
DATE    :       YEAR '-' MONTH '-' DAY 'T' HOUR ':' MIN ':' SEC;
SEPARATOR       :       ' '? '*' ' '?;

fragment        YEAR    :       [1-2][0-9][0-9][0-9];
fragment        MONTH   :       [0-1][0-9];
fragment        DAY             :       [0-3][0-9];
fragment        HOUR    :       [0-2][0-9];
fragment        MIN             :       [0-6][0-9];
fragment        SEC             :       [0-6][0-9];

WS      :       [ \t\r\n]+ -> skip ; 