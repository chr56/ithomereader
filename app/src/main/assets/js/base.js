let images = document.querySelectorAll('img.lazy');
let imageUrls = Array.prototype.slice.call(images).map(img => img.src);

Array.prototype.slice.call(images).forEach(function (img) {
    img.addEventListener("click", function (e) {
        e.preventDefault();
        e.stopPropagation();
        var target = e.target;
        if (target.classList.contains("loaded")) {
            JSInterface.openInViewer(target.src, imageUrls);
        } else {
            target.src = target.dataset.original;
            target.classList.add("loaded");
        }
    });
});
