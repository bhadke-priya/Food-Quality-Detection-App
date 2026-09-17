(function() {
    // Click the FBO Search tab
    var tab = document.getElementById("governmentAgencies1");
    if (!tab) {
//        window.Android.showToast("FBO Search tab not found.");
        return;
    }
    tab.click();

    // Wait for the form to be available and fill the FSSAI number
    setTimeout(function() {
        var input = document.querySelector('input[name="fssaiLicenseNo"]');
        if (!input) {
//            window.Android.showToast("FSSAI input field not found.");
            return;
        }
        input.value = "__FSSAI_NUMBER__"; // Replaced dynamically in WebViewFragment

        var submitBtn = document.querySelector('button[type="submit"], input[type="submit"]');
        if (!submitBtn) {
//            window.Android.showToast("Submit button not found.");
            return;
        }
        submitBtn.click();
    }, 1500);
})();