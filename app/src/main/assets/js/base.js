var imgs = document.getElementsByTagName("img")
Array.prototype.slice.call(imgs).forEach(function(img) {
    img.addEventListener("click", function(e) {
        e.preventDefault();
        e.stopPropagation();
        var target = e.target;
        if(target.classList.contains("loaded")) {
            JSInterface.openInViewer(target.src);
        } else {
            target.src = target.dataset.original;
            target.classList.add("loaded");
        }
    });
});
