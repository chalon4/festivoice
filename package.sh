#!/usr/bin/env bash

name="festivoice"
version="0.1.0"  # FIX src/main/webapp/server.html
server="${name}d-${version}"
src="${name}-src-${version}"

mvn package || exit 1

tar czvf festivoice.net.tar.gz festivoice.net

rm -rf "${server}.zip" "${server}"
mkdir -p "${server}/lib"
mkdir -p "${server}/webapp/WEB-INF"
mkdir -p "${server}/webapp/img"
mkdir -p "${server}/webapp/pkg"
cp -f LICENSE "${server}/"
cp -f NOTICE "${server}/"
cp -f target/festivoice.jar "${server}/webapp/pkg/"
cp -f lib/winstone-lite-*.jar "${server}/lib/winstone-lite.jar"
cp -f src/main/webapp/jnlp.xsl "${server}/webapp/"
cp -f src/main/webapp/style.css "${server}/webapp/"
cp -f src/main/webapp/index.xsl "${server}/webapp/"
cp -f src/main/webapp/staff.html "${server}/webapp/"
cp -f src/main/webapp/client.html "${server}/webapp/"
cp -f src/main/webapp/server.html "${server}/webapp/"
cp -f src/main/webapp/pkg/festivoice.net.jnlp "${server}/webapp/pkg/"
cp -f src/main/webapp/img/background.png "${server}/webapp/img/background.png"
cp -f src/main/webapp/img/grayback.png "${server}/webapp/img/grayback.png"
cp -f src/main/webapp/img/logo.png "${server}/webapp/img/logo.png"
cp -f src/main/webapp/WEB-INF/web.xml "${server}/webapp/WEB-INF/"
cp -f festivoiced "${server}/"
cp -f festivoice.net.tar.gz "${server}/webapp/pkg/"
chmod 755 "${server}/festivoiced"
tar czvf "${server}.tar.gz" "${server}"

rm -rf "${src}.zip" "${src}"
mkdir -p "${src}/src/main/java/net/festivoice"
mkdir -p "${src}/src/main/java/org/xiph/speex/spi"
mkdir -p "${src}/src/main/webapp/WEB-INF"
mkdir -p "${src}/src/main/webapp/img"
mkdir -p "${src}/src/main/webapp/pkg"
mkdir -p "${src}/lib"
cp -f LICENSE "${src}/"
cp -f NOTICE "${src}/"
cp -f pom.xml "${src}/"
cp -f package.sh "${src}/"
cp -f LICENSE "${src}/"
cp -f festivoice.net "${src}/"
cp -f festivoiced "${src}/"
cp -f lib/winstone-lite-*.jar "${src}/lib/"
cp -f src/main/java/net/festivoice/*.java "${src}/src/main/java/net/festivoice/"
cp -f src/main/java/org/xiph/speex/*.java "${src}/src/main/java/org/xiph/speex/"
cp -f src/main/java/org/xiph/speex/spi/*.java "${src}/src/main/java/org/xiph/speex/spi/"
cp -f src/main/webapp/jnlp.xsl "${src}/src/main/webapp/"
cp -f src/main/webapp/style.css "${src}/src/main/webapp/"
cp -f src/main/webapp/index.xsl "${src}/src/main/webapp/"
cp -f src/main/webapp/staff.html "${src}/src/main/webapp/"
cp -f src/main/webapp/client.html "${src}/src/main/webapp/"
cp -f src/main/webapp/server.html "${src}/src/main/webapp/"
cp -f src/main/webapp/pkg/festivoice.net.jnlp "${src}/src/main//webapp/pkg/"
cp -f src/main/webapp/img/background.png "${src}/src/main/webapp/img/background.png"
cp -f src/main/webapp/img/grayback.png "${src}/src/main/webapp/img/grayback.png"
cp -f src/main/webapp/img/logo.png "${src}/src/main/webapp/img/logo.png"
cp -f src/main/webapp/WEB-INF/web.xml "${src}/src/main/webapp/WEB-INF/"
zip -r "${src}.zip" "${src}"
