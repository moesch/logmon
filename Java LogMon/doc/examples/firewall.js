/*
 * Firewall condition script
 *
 * Alert if more as 5 deny from same ip. Alert every 5 occurrence
 */

var src=occurrence.groups[3];
occurrence.maxage=3600;

db.save("FW"+src);

if(occurrence.repeat >=5  && occurrence.repeat % 5 ==0){
	status=true;
}
