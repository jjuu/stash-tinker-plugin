AJS.toInit(function () {
    // your initialisation code here
//	AJS.log("code here.asdf toInit????");
	AJS.$("#select-project").auiSelect2();
	AJS.$("#select-respository").auiSelect2();
	
	var error = AJS.$("#tinkerError").val();
	
	error = error.replace("{", "").replace("}", "").replace("[", "").replace("]", "").split(":");
//	AJS.log("Errors: "+error);
	
	if (error.length != 0) {
		var errorMsg = error[1];
		if (AJS.$.type(errorMsg) != "undefined") {
			AJS.messages.error({
			    title: 'Watch Out!',
			    body: '<p>' + errorMsg + '.</p>'
			});
		}
	}

	// Retrieve the repositories of a project
	var retrieveReposURL = AJS.contextPath() + "/plugins/servlet/tinkerConfig/retrieveRepos";

	// select project 
	AJS.$("#select-project").bind("change", function(e) {
		var projKey = this.value;
		AJS.$.get(retrieveReposURL, {
			"projectKey": projKey 
		}, function(reposList) {
			var selectReposElt = AJS.$("#select-respository");
			selectReposElt.empty();
			
			AJS.$('#s2id_select-respository .select2-chosen').html('');
			
			AJS.$.each(reposList, function(i, reposName) {
				selectReposElt.append('<option value="'+reposName+'">'+reposName+'</option>');
			});
		});
	});
	
	// check before submit
	AJS.$("#tinker-form").submit(function(e) {
		var projName = AJS.$('#s2id_select-project .select2-chosen').text();
		var reposName = AJS.$('#s2id_select-respository .select2-chosen').text();
		
		projName = AJS.$.trim(projName);
		reposName = AJS.$.trim(reposName);
		
//		AJS.log("tinker-form-> " + projName + ":" + reposName);
		
		if (reposName == '') {
			AJS.messages.error({
			    title: 'Watch Out!',
			    body: '<p>Repository cannot be empty.</p>'
			});
			
			return false;
		}
		
		var formData = AJS.$(this).serialize();
		var action = AJS.$(this).attr("action");

		AJS.$.post(action, formData, function(response) {
			
		});
		
	});

	/*AJS.$('.feature-owners').click(function() {
		var projKey = AJS.$(this).data("projKey");
		var reposName = AJS.$(this).data("reposName");
		
		var fileCSV = AJS.$(this).parents(".feature-form").find('input.feature-owners-csv').val();
		if (fileCSV == "") {
			AJS.messages.error({
			    title: 'Watch Out!',
			    body: '<p>Upload a csv file first.</p>'
			});

			return false;
		} else if (fileCSV.split('.').pop() != 'csv') {
			AJS.messages.error({
			    title: 'Watch Out!',
			    body: '<p>Only accept csv file.</p>'
			});

			return false;
		}
		AJS.log(AJS.$(this).parents('.feature-form').submit);
		AJS.$(this).parents('.feature-form').submit();
	})*/
	
	// Config the feature owners
	/*AJS.$("form.feature-form").submit(function(e) {
		var projKey = AJS.$(this).data("projKey");
		var reposName = AJS.$(this).data("reposName");
		
		var fileCSV = AJS.$(this).parents(".feature-form").find('input.feature-owners-csv').val();
		if (fileCSV == "") {
			AJS.messages.error({
			    title: 'Watch Out!',
			    body: '<p>Upload a csv file first.</p>'
			});

			return false;
		} else if (fileCSV.split('.').pop() != 'csv') {
			AJS.messages.error({
			    title: 'Watch Out!',
			    body: '<p>Only accept csv file.</p>'
			});

			return false;
		}

		var uploadCSVURL = AJS.contextPath() + "/plugins/servlet/tinkerConfig/uploadCSV";
		
		var formElt = AJS.$("#feature-owners").parents(".feature-form");
		AJS.log("tinker-form-> " + AJS.$("#feature-owners").parents(".feature-form"));
		
		var formData = formElt.serialize();
		
		formElt.submit(function(e) {
			e.preventDefault();
			var formData = formElt.serialize();
			var action = formElt.attr("action");
			AJS.log("tinker-form-action-> " + action);
			AJS.log("tinker-form-data-> " + formData);
			
			AJS.$.post(action, formData, function(response) {
//				window.location.reload();
			});
		});
		
		
	});*/

	// Remove a repository from a project
	var removeReposURL = AJS.contextPath() + "/plugins/servlet/removeTinkerRepos";
	
	// Remove the repository
	AJS.$(".proj-repos-remove").bind("click", function() {
		var projKey = AJS.$(this).data("projKey");
		var reposName = AJS.$(this).data("reposName");
		
		AJS.$.post(removeReposURL, {
			"projectKey": projKey,
			"repository": reposName
		}, function() {
			
		});
	});

	// Test click button
	AJS.$("#bt_test").bind("click", function() {
		var projName = AJS.$('#s2id_select-project .select2-chosen').text();
		var reposName = AJS.$('#s2id_select-respository .select2-chosen').text();
		
		projName = AJS.$.trim(projName);
		reposName = AJS.$.trim(reposName);
		
		AJS.messages.generic({
		    title: 'This is a title in a default message.',
		    body: '<p> And this is just content in a Default message.</p>'
		});
	});

	// Dialogs
	AJS.$(".edit-feature-owner").bind("click", function() {
		AJS.dialog2("#feature-dialog").show();
	});
	
	AJS.$(".edit-feature-reviewer").bind("click", function() {
		AJS.dialog2("#reviewer-dialog").show();
	});
});
