(ns track.validation)

(defn- any-whitespace?
  "Check that there isn't any whitespace in the string."
  [^String string]
  (not (not-any? #(= \space %) string)))

(defmacro compare-count
  [comparator string num]
  `(~comparator (count ~string) ~num))

(defn- invalid-max-length?
  [^String string ^Integer max]
  (compare-count > string max))

(defn- invalid-min-length?
  [^String string ^Integer min]
  (compare-count < string min))

(defn username
  [^String username]
  (cond
   (empty? username)
   (throw (Exception. "Username can't be blank."))
   (any-whitespace? username)
   (throw (Exception. "No whitespace allowed in username."))
   (invalid-min-length? username 5)
   (throw (Exception. "The minimum length of a username is 6 characters."))
   (invalid-max-length? username 15)
   (throw (Exception. "The maximum length of a username is 15 characters."))
   :else true))

