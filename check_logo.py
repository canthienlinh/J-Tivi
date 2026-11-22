import json, io
P = r'F:\PROJECT\J-TV\playlist\channels.json'
d = json.load(open(P, encoding='utf-8'))
out = io.open(r'F:\PROJECT\J-TV\logo_out.txt', 'w', encoding='utf-8')
for c in d:
    out.write("%s ||| %s\n" % (c.get("name",""), c.get("logo","")))
out.close()
