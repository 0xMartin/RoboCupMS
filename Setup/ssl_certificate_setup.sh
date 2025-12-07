# 1) zpusob - self signed certificate 
# vygenerovani ssl certifikatu a vlozeni do keystoru
keytool -genkeypair -alias springboot -keyalg RSA -keysize 4096 -storetype PKCS12 -keystore robocupms.p12 -validity 3650 -storepass f4R03eRRG3

# 2) zpusob - vygenerovani certifikatu z privatniho klice a csr
openssl x509 -req -days 365 -in robocupms.csr -signkey robocupms.key -out robocupms.crt
# heslo: f4R03eRRG3
openssl pkcs12 -export -in robocupms.crt -inkey robocupms.key -out robocupms.p12 -name springboot
