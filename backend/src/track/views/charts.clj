(ns track.views.charts
  (:use hiccup.def
        [clojure.string :only [capitalize]]
        [clojure.data.json :only [json-str]]
        [track.model :only [fetch-datapoints]]))

(defn add-columns
  [attributes]
  (for [attr attributes]
    (str "data.addColumn('number', '" (capitalize attr) "';")))

(defn add-rows
  "TODO: Only handles data for one attribute."
  [datapoints]
  (let [rows (json-str
              (map #(cons (:time %) [(:measurement %)]) datapoints))]
    (str "data.addRows([" rows "]);")))

(defn add-title
  [title]
  (str "var options = { title: '" title "'};"))

(defhtml device [device]
  (let [datapoints (fetch-datapoints device)]
    [:script {:type "text/javascript"}
     (str
      "google.load(\"visualization\", \"1\", {packages:[\"corechart\"]});
      google.setOnLoadCallback(drawChart);
      function drawChart() {
        var data = new google.visualization.DataTable();
      data.addColumn('string', 'Time');"
      (add-columns (:attribute (map first datapoints)))
      (add-rows datapoints)
      (add-title (str "Device " (:device_id (first datapoints))))
      "var chart = new google.visualization.LineChart(document.getElementById('device_div'));
        chart.draw(data, options);}")]
    [:div {:id "device_div" :style "height: 500px;"}]))


"google.load(\"visualization\", \"1\", {packages:[\"corechart\"]});
      google.setOnLoadCallback(drawChart);
      function drawChart() {
        var data = new google.visualization.DataTable();
        data.addColumn('string', 'Year');
        data.addColumn('number', 'Sales');
        data.addColumn('number', 'Expenses');
        data.addRows([
          ['2004', 1000, 400],
          ['2005', 1170, 460],
          ['2006',  860, 580],
          ['2007', 1030, 540]
        ]);

        var options = {
          title: 'Device 1'
        };

        var chart = new google.visualization.LineChart(document.getElementById('device_div'));
        chart.draw(data, options);
      }"
