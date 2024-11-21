document.addEventListener('DOMContentLoaded', function () {
    const apiEndpoint = '/ds_api/total_service_statistics';

    fetch(apiEndpoint, {
        method: 'POST', 
        headers: {
            'Content-Type': 'application/json', 
        },
        body: JSON.stringify({}),
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {           
            document.getElementById('total-short-urls-created').textContent = data.totalShortUrlsCreated;
            document.getElementById('total-short-urls-used').textContent = data.totalShortUrlsUsed;
            document.getElementById('total-image-albums-created').textContent = data.totalImageAlbumsCreated;
            document.getElementById('total-image-albums-visited').textContent = data.totalImageAlbumsVisited;
            document.getElementById('total-images-created').textContent = data.totalImagesCreated;
            document.getElementById('total-images-visited').textContent = data.totalImagesVisited;
        })
        .catch(error => {
            console.error('Error fetching statistics:', error);
        });
});
