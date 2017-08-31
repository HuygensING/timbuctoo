java_plugin(
    name = "immutables_processor",
    deps = [
        "//third_party:org_immutables_value",
    ],
    processor_class = "org.immutables.processor.ProxyProcessor",
)

java_library(
    name = "rml",
    plugins = [
      ":immutables_processor"
    ],
    srcs = glob([
        "**/*.java",
    ]),
    deps = [
      "//third_party:org_immutables_value",
      "//third_party:org_apache_commons_commons_lang3",
      "//timbuctoo-instancev4/src/main/java/nl/knaw/huygens/timbuctoo/util:utils",
      "//third_party:com_google_guava_guava",
      "//third_party:org_slf4j_slf4j_api",
    ],
    visibility = ["//visibility:public"],
)
