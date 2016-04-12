


AJS.$(document).ready(function($) {
    return function() {
    	var reviewer = '<li class="select2-search-choice">\
				    		<div>\
					    		<div class="avatar-with-name" title="Mao Yunlong-B43258">\
					    			<span class="aui-avatar aui-avatar-xsmall user-avatar" data-username="B43258">\
					    				<span class="aui-avatar-inner"><img src="/users/b43258/avatar.png?s=32" alt="Mao Yunlong-B43258"></span>\
					    			</span>Mao Yunlong-B43258\
				    			</div>\
				    		</div>\
				    		<a href="#" onclick="return false;" class="select2-search-choice-close" tabindex="-1"></a>\
			    		</li>';
    	
    	
    	jQuery('#s2id_reviewers ul').prepend(reviewer)

    	jQuery('.select2-search-choice-close').click(function(){jQuery('.select2-search-choice').remove()})

    	
    	
    };
}(AJS.$));