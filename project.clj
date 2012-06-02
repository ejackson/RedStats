(defproject redstats "0.1-SNAPSHOT"
  :description "Use reduces for a stats libary"
  :dependencies [[org.clojure/clojure "1.5.0-master-SNAPSHOT"]
                 [org.codehaus.jsr166-mirror/jsr166y "1.7.0"]]
  :repositories {"sonatype" {:url "http://oss.sonatype.org/content/repositories/releases"
                             :snapshots false
                             :releases {:checksum :fail :update :always}}
                 "sonatype-snapshots" {:url "http://oss.sonatype.org/content/repositories/snapshots"
                                       :snapshots true
                                       :releases {:checksum :fail :update :always}}})
