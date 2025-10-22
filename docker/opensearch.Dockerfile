# opensearch
FROM opensearchproject/opensearch:3.0.0
RUN /usr/share/opensearch/bin/opensearch-plugin install -b analysis-icu
RUN /usr/share/opensearch/bin/opensearch-plugin install -b \
  https://github.com/aparo/opensearch-analysis-pinyin/releases/download/3.0.0/opensearch-analysis-pinyin.zip

# ...


