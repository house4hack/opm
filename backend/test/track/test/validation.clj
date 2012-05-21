(ns track.test.validation
  (:use [clojure.test]
        [track.validation]))

(deftest test-username
  (testing "Username can't be blank"
    (is (thrown-with-msg? Exception #"blank"
          (username ""))))
  (testing "Minimum length of username is 5"
    (is (thrown-with-msg? Exception #"minimum"
          (username "1234")))
    (is (true? (username "123456"))))
  (testing "Maximum length of username is 15"
    (is (thrown-with-msg? Exception #"maximum"
          (username "1234567890123456")))
    (is (true? (username "123456789012345"))))
  (testing "No whitespace"
    (is (thrown-with-msg? Exception #"whitespace"
          (username "1234 56")))
    (is (thrown-with-msg? Exception #"whitespace"
          (username " 123456")))
    (is (thrown-with-msg? Exception #"whitespace"
          (username "123456 ")))))
