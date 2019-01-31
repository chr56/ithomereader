var imgs = document.getElementsByTagName('img');
for(var i = 0; i < imgs.length; i++) {
    imgs[i].addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        var target = e.target;
        if(target.classList.contains('loaded')) {
            JSInterface.openInViewer(target.src);
        } else {
            target.src = target.dataset.original;
            target.classList.add('loaded');
        }
    })
}