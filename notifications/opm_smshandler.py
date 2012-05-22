import httplib, urllib, _mysql

def sendsms( msg, cellno ):
    # replace username and password with your own bulksms credentials
	params = urllib.urlencode({'username': 'username', 'password': 'password', 'message': msg, 'msisdn': cellno })
	headers = {"Content-type": "application/x-www-form-urlencoded","Accept": "text/plain"}
	conn = httplib.HTTPConnection("bulksms.2way.co.za:5567")
	conn.request("POST", "/eapi/submission/send_sms/2/2.0", params, headers)
	response = conn.getresponse()
	print response.status, response.reason
	data = response.read()
	print data
	
# connect to db

# replaces these non-working usernames and passwords with your own account
db = _mysql.connect(host="host",port=3306,user="user",passwd="password",db="opm")
db.query("""select queue.id,pumps.name,contacts.number, status.voltage, queue.status from queue,pumps,contacts, status where queue.sent=0 and pumps.id=queue.pump and contacts.id=queue.contact and status.pump = pumps.id""")
r=db.use_result()

data = r.fetch_row()
status = []

# replace the info with your own
updateStatus = _mysql.connect(host="host",port=3306,user="user",passwd="password",db="opm")

while len(data) > 0:
#	print data
	
	if data[0][4] == 'on':
		sendsms( "Power Status Changed to ON on Pump " + data[0][1] + ", Voltage :" + data[0][3] + "V", data[0][2] )
	if data[0][4] == 'off':
		sendsms( "Power Status Changed to OFF on Pump " + data[0][1] + ", Voltage :" + data[0][3] + "V", data[0][2] )
	if data[0][4] == 'low':
		sendsms( "Power dip on Pump " + data[0][1] + ", Voltage :" + data[0][3] + "V", data[0][2] )
	if data[0][4] == 'high':
		sendsms( "Power surge on Pump " + data[0][1] + ", Voltage :" + data[0][3] + "V", data[0][2] )
	
	updateStatus.query( "update queue set sent=1 where id=" + data[0][0] );
	data = r.fetch_row()

