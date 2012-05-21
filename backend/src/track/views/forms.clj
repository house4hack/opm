(ns track.views.forms
  (:use [hiccup.form]
        [hiccup.def]))


(defhtml control-group
  "Control group for new information or editing existing."
  ([text name value help]
      [:div.control-group
       (label {:class "control-label"} name text)
       [:div.controls
        (text-field {:class "input-xlarge"} name value)
        [:p.help-block help]]])
  ([text name help]
     (control-group text name "" help)))

(def blanks (cycle [""]))

(defhtml registration
  [post]
  (form-to {:class "form-vertical well"} [:post post]
           [:fieldset
            [:legend "Register User"]
            [:div.control-group
             (label {:class "control-label"} :username "Username")
             [:div.controls
              (text-field {:class "input-xlarge"} :username)
              [:p.help-block "Must be between 5 and 16 characters."]]]
            [:div.control-group
             (label {:class "control-label"} :password "Password")
             [:div.controls
              (password-field {:class "input-xlarge"} :password)
              [:p.help-block "Please enter a password."]]]
            [:div.control-group
             (label {:class "control-label"} :password2 "Repeat Password")
             [:div.controls
              (password-field {:class "input-xlarge"} :password2)
              [:p.help-block "To make sure the password was entered correctly."]]]
            [:div.control-group
             (label {:class "control-label"} :name "Full Name")
             [:div.controls
              (text-field {:class "input-xlarge"} :name)
              [:p.help-block "(Optional)"]]]
            [:div.control-group
             (label {:class "control-label"} :email "Email Address")
             [:div.controls
              (email-field {:class "input-xlarge"} :email)
              [:p.help-block "(Optional)"]]]
            [:div.form-actions
             (submit-button {:class "btn btn-primary"} "Save Changes") " "
             (reset-button {:class "btn"} "Cancel")]]))

(defhtml vertical
  ([post legend labels fields values helptexts]
     (form-to {:class "form-vertical well"} [:post post]
              [:fieldset
               [:legend legend]
               (map control-group labels fields values helptexts)
               [:div.form-actions
                (submit-button {:class "btn btn-primary"} "Save Changes") " "
                (reset-button {:class "btn"} "Cancel")]]))
  ([post legend labels fields helptexts]
     (vertical post legend labels fields blanks helptexts)))

(defhtml modal
  [{:keys [post legend labels fields values helptexts]}]
  (form-to [:post post]
           [:div.modal-header
            [:a.close {:data-dismiss "modal"} "X"]
            [:h2 legend]]
           [:div.modal-body
            (map control-group labels fields values helptexts)]
           [:div.modal-footer
            (submit-button {:class "btn btn-primary"} "Save Changes") " "
            (reset-button {:class "btn close"} "Cancel")]))

