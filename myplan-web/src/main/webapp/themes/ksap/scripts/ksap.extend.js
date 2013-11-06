// TODO: Redefinition of coerceValue function not needed after bug resolved - https://jira.kuali.org/browse/KULRICE-9883
function coerceValue(name) {
    var value = "";
    var nameSelect = "[name='" + escapeName(name) + "']";
    // when group is opened in lightbox make sure to get the value from field in the lightbox
    // if that field is in the lightbox
    var parent = document;
    if (jQuery(nameSelect, jQuery(".fancybox-wrap")).length) {
        parent = jQuery(".fancybox-wrap");
    }
    if (jQuery(nameSelect + ":checkbox", parent).length == 1) {
        value = jQuery(nameSelect + ":checked", parent).val();
    }
    else if (jQuery(nameSelect + ":checkbox", parent).length > 1) {
        value = [];
        jQuery(nameSelect + ":checked", parent).each(function () {
            value.push(jQuery(this).val());
        });
    }
    else if (jQuery(nameSelect + ":radio", parent).length) {
        value = jQuery(nameSelect + ":checked", parent).val();
    }
    else if (jQuery(nameSelect, parent).length) {
        if (jQuery(nameSelect, parent).hasClass("watermark")) {
            jQuery.watermark.hide(nameSelect, parent);
            value = jQuery(nameSelect, parent).val();
            jQuery.watermark.show(nameSelect, parent);
        }
        else {
            value = jQuery(nameSelect, parent).val();
        }
    }
    if (value == null) {
        value = "";
    }
    if (value == "true") {
        value = true
    }
    if (value == "false") {
        value = false
    }

    return value;
}
// TODO: Redefinition of setupProgressiveCheck function not needed if improvement resolved - https://jira.kuali.org/browse/KULRICE-9992
function setupProgressiveCheck(controlName, disclosureId, baseId, condition, alwaysRetrieve, methodToCall, onKeyUp) {
    if (!baseId.match("\_c0$")) {
        var theControl = jQuery("[name='" + escapeName(controlName) + "']");
        var eventType = 'change';

        if (onKeyUp && (theControl.is("textarea") || theControl.is("input[type='text'], input[type='password']"))) {
            eventType = 'ready focus keyup paste cut contextmenu mouseout blur';
        }

        theControl.on(eventType, function () {
            var refreshDisclosure = jQuery("#" + disclosureId);
            if (refreshDisclosure.length) {
                var displayWithId = disclosureId;

                if (condition()) {
                    if (refreshDisclosure.data("role") == "placeholder" || alwaysRetrieve) {
                        retrieveComponent(disclosureId, methodToCall);
                    }
                    else {
                        refreshDisclosure.addClass(kradVariables.PROGRESSIVE_DISCLOSURE_HIGHLIGHT_CLASS);
                        refreshDisclosure.show();

                        if (refreshDisclosure.parent().is("td")) {
                            refreshDisclosure.parent().show();
                        }

                        refreshDisclosure.animate({backgroundColor: "transparent"}, 6000);

                        //re-enable validation on now shown inputs
                        hiddenInputValidationToggle(disclosureId);

                        var displayWithLabel = jQuery(".displayWith-" + displayWithId);
                        displayWithLabel.show();
                        if (displayWithLabel.parent().is("td") || displayWithLabel.parent().is("th")) {
                            displayWithLabel.parent().show();
                        }
                    }
                }
                else {
                    refreshDisclosure.hide();

                    // ignore validation on hidden inputs
                    hiddenInputValidationToggle(disclosureId);

                    var displayWithLabel = jQuery(".displayWith-" + displayWithId);
                    displayWithLabel.hide();
                    if (displayWithLabel.parent().is("td") || displayWithLabel.parent().is("th")) {
                        displayWithLabel.parent().hide();
                    }
                }
            }
        });

        if (onKeyUp && (theControl.is("textarea") || theControl.is("input[type='text'], input[type='password']"))) {
            theControl.trigger("ready");
        }
    }
}

function setupLightboxForm() {
    jQuery(".fancybox-inner").children().wrap("<form style='margin:0; padding:0; overflow:auto; height:100%' id='kualiLightboxForm' class='uif-lightbox'>");

    var kualiLightboxForm = jQuery('#kualiLightboxForm');
    setupValidator(kualiLightboxForm);
}