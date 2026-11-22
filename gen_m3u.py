import json, io
P = r'F:\PROJECT\J-TV\playlist\channels.json'
M = r'F:\PROJECT\J-TV\playlist\jtv_clean.m3u'
d = json.load(open(P, encoding='utf-8'))
out = io.open(M, 'w', encoding='utf-8')
out.write("#EXTM3U\n")
for c in d:
    tvg = c.get("tvgId", "") or ""
    logo = c.get("logo", "") or ""
    grp = c.get("group", "") or ""
    name = c.get("name", "")
    out.write('#EXTINF:-1 tvg-id="%s" tvg-logo="%s" group-title="%s",%s\n' % (tvg, logo, grp, name))
    ua = c.get("userAgent", "") or ""
    ref = c.get("referer", "") or ""
    if ua:
        out.write("#EXTVLCOPT:http-user-agent=%s\n" % ua)
    if ref:
        out.write("#EXTVLCOPT:http-referrer=%s\n" % ref)
    out.write(c.get("url", "") + "\n")
out.close()
io.open(r'F:\PROJECT\J-TV\gen_out.txt','w',encoding='utf-8').write("channels: %d\n" % len(d))
