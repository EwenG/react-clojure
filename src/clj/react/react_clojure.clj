(ns clj.react.react-clojure
  "Utilities to convert JSX into javascript"
  (:import [javax.script ScriptEngine ScriptEngineManager]))

(comment
  "The JSXTransformer script has been modified to be compatible with the rhino engine embedded in the JRE: Every use of 'static' as a property has been renamed to 'static2'. Every use of 'char' as a parameter has been renamed to 'char2'

We cannot use escaped quotes (\") when passing a string to a javax.script.ScriptEngine object for whatever reason. Thus all escaped quotes are replaced with a single quote.

A @jsx docblock is appended at the beginning of the JSX string to be transformed. Maybe this should be reworked? Detect if a JSX docblock is already present?

TODO: No no validation is perfomed on the input JSX string."
)

(defonce engine (.getEngineByName (ScriptEngineManager.) "javascript"))

(.eval engine (clojure.java.io/reader (clojure.java.io/resource "JSXTransformer-0.8.0.js")))


(defn normalize-string
  "Replaces every occurence of the escaped quote character (\") in the input string into a single quote character. Also replaces \n by a whitespace.

Example: 
 (replace-quotes \"<html><body><div id=\"my-id\"></div></body></html>\") -> 
\"<html><body><div id='my-id'></div></body></html>\""
  [s]
  (-> s (clojure.string/replace "\"" "'") (clojure.string/replace "\n" " ")))

(defn html->jsx [html]
  "Transform the input JSX string into javascript.

Note that this function appends a @jsx docblock at the beginning of the input JSX string.
Also, currently no validation is perfomed on the input string. The behavior is unspecified if the input string is bad formatted !

Example: 
 (html->jsx \"<html><body><div id=\"my-id\"></div></body></html>\") ->
\"/** @jsx React.DOM */ React.DOM.html(null, React.DOM.body(null, React.DOM.div( {id:\"my-id\"})))\""
  (let [transformed-jsx 
        (->> (normalize-string html) 
             (format "global.JSXTransformer.transform(\"/** @jsx React.DOM */ %s\")")
             (.eval engine))]
    (get transformed-jsx "code")))