AJS.toInit(function () {

    var getUrlParameter = function getUrlParameter(sParam) {
        var sPageURL = decodeURIComponent(window.location.search.substring(1)),
            sURLVariables = sPageURL.split('&'),
            sParameterName,
            i;

        for (i = 0; i < sURLVariables.length; i++) {
            sParameterName = sURLVariables[i].split('=');

            if (sParameterName[0] === sParam) {
                return sParameterName[1] === undefined ? true : sParameterName[1];
            }
        }
    };
    
    function doAutoReviewers(coreId) {
    	var userAvatarURL = AJS.contextPath() + '/rest/api/latest/users?avatarSize=32&start=0&' + coreId;
    	
    	AJS.$.get(userAvatarURL, function(avatars) {
    		if (avatars['size'] == 0) {
    			AJS.log('No avatar found 1');
    			return;
    		}

    		var avatarList = avatars['values'];
    		var avatar = null;
    		for (var i = 0 ; i < avatarList.length ; i++) {
    			
    			if (avatarList[i]['name'].toLowerCase() == coreId.toLowerCase()) {
    				avatar = avatarList[i];
    				break;
    			}
    		}

    		if (avatar == null) {
    			AJS.log('No avatar found 2');
    			return;
    		}

    		var avatarURL = avatar['avatarUrl'];
    		var displayName = avatar['displayName'];
    		var userFullname = displayName + '-' + coreId;
			var reviewerHtml = '<li class="select2-search-choice">\
                <div>\
                    <div class="avatar-with-name" title="' + userFullname + '">\
                        <span class="aui-avatar aui-avatar-xsmall user-avatar" data-username="' + coreId + '">\
                            <span class="aui-avatar-inner"><img src="' + avatarURL + '" alt="' + userFullname + '"></span>\
                        </span>'+userFullname+'\
                    </div>\
                </div>\
                <a href="#" onclick="return false;" class="select2-search-choice-close" tabindex="-1"></a>\
            </li>';
			
			doAppendReviewElt(coreId, reviewerHtml);
    	});
    }
    
    function doAppendReviewElt(coreID, reviewer) {

		// Set value of the form element
		var reviewerInfoStr = AJS.$('#reviewers').val();
		var reviewerList = reviewerInfoStr.split('|!|');
		
		if (reviewerList.indexOf(coreID) == -1) {
			// If the user not exist append it
			var reviewerElt = AJS.$(reviewer);
			
			AJS.$('#s2id_reviewers ul').prepend(reviewerElt);
			if (reviewerInfoStr == '') {
				reviewerInfoStr = coreID;
			} else {
				reviewerInfoStr += '|!|' + coreID;
			}

			AJS.$('#reviewers').val(reviewerInfoStr);

			AJS.$('.select2-search-choice-close').click(function() {
				var userName = AJS.$(this).parent('.select2-search-choice').find('.user-avatar').data('username');

				AJS.$(this).parent('.select2-search-choice').remove();

				var reviewVal = '';
				var reviewerList = AJS.$('#reviewers').val().split('|!|');
				for (var j = 0 ; j < reviewerList.length ; j++) {
					if (reviewerList[j] != userName && reviewerList[j] != "") {
						reviewVal += '|!|' + reviewerList[j];
					}
				}

				reviewVal = reviewVal.replace('|!|', '');
				AJS.$('#reviewers').val(reviewVal);
			});
		}
	}

	AJS.$('#show-create-pr-button').click(function() {
		var url = window.location.href.split('?')[0];
		var projArr = url.match(/projects.+repos/);
		if (projArr == null) {
			return;
		} else {
			var projKey = projArr[0].replace('projects/', '').replace('/repos', '');
		}
		
		var reposArr = url.match(/repos.+pull-requests/);
		if (reposArr == null) {
			return;
		} else {
			var reposName = reposArr[0].replace('repos/', '').replace('/pull-requests', '')
		}
		
		var targetBranch = getUrlParameter('targetBranch').replace('refs/heads/', '');
		var sourceBranch = getUrlParameter('sourceBranch').replace('refs/heads/', '');

		var reviewerUrl = AJS.contextPath() + '/plugins/servlet/tinker/reviews?tb=' + encodeURIComponent(targetBranch)
																				+ '&projectKey=' + projKey
																				+ '&repository=' + reposName;
		AJS.log('reviewerUrl: ' + reviewerUrl);
		AJS.$.get(reviewerUrl, function(reviewers) {
			for (var i = 0 ; i < reviewers.length ; i++) {
				doAutoReviewers(reviewers[i]);
			}
		});
	});
});
