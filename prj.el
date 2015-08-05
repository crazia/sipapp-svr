(jde-project-file-version "1.0")
(jde-set-variables
'(jde-compile-option-command-line-args
(quote ("-Xlint:all" "-Xlint:-serial"))))
(require 'pom-parser)
  (with-pom ()
  (pom-set-jde-variables *pom-node*))
