# Dockerfile for Hyperledger blockchain-explorer image

FROM node:8.11.4-stretch

EXPOSE 8080

# install maintain tools
#RUN npm install bower grunt-cli graceful-fs@4.1.5 minimatch@3.0.2 -g

# clone latest code from github
RUN git clone --single-branch -b release-3.6 --depth 1 https://github.com/hyperledger/blockchain-explorer

WORKDIR /blockchain-explorer

#RUN echo '{ "allow_root": true }' > /root/.bowerrc
#RUN echo '{ "allow_root": true }' > .bowerrc

# Modify config.json to update the value of channels/mysql/tls
# Or just mount external one inside

RUN npm install -g grpc@1.10.1 --registry=http://registry.npm.taobao.org --unsafe-perm=true --allow-root \
	&& npm install --registry=http://registry.npm.taobao.org

RUN cd client && npm install --registry=http://registry.npm.taobao.org

#RUN cd /blockchain-explorer/app/test && npm install --registry=http://registry.npm.taobao.org

#VOLUME /blockchain-explorer

CMD ["node", "main.js"]
