document.addEventListener('DOMContentLoaded', function () {
    const totalApiEndpoint = '/ds_api/total_service_statistics';
    const recentApiEndpoint = '/ds_api/recent_statistics';

    // Fetch total statistics
    fetch(totalApiEndpoint, {
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
            document.getElementById('total-web-service').textContent = data.totalWebServiceCount || 0;
            document.getElementById('total-short-urls-created').textContent = data.totalShortUrlsCreated || 0;
            document.getElementById('total-short-urls-used').textContent = data.totalShortUrlsUsed || 0;
            document.getElementById('total-image-albums-created').textContent = data.totalImageAlbumsCreated || 0;
            document.getElementById('total-image-albums-visited').textContent = data.totalImageAlbumsVisited || 0;
            document.getElementById('total-images-created').textContent = data.totalImagesCreated || 0;
            document.getElementById('total-images-visited').textContent = data.totalImagesVisited || 0;
            document.getElementById('total-css-js-minified').textContent = data.totalCssJsMinified || 0;
            // document.getElementById('total-webp-converted').textContent = data.totalWebpConverted || 0;
        })
        .catch(error => {
            console.error('Error fetching total statistics:', error);
        });

    // Fetch recent statistics
    fetch(recentApiEndpoint, {
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
        .then(responseData => {
            const data = responseData.data;

            const createdData = data.created || initializeEmptyData();
            const usedData = data.used || initializeEmptyData();

            drawChart('created-short-url-chart', '短網址建立 - Short URL Creation', alignData(createdData.url), 'rgb(48,255,48)');
            drawChart('created-album-chart', '圖片相簿建立 - Image Album Creation', alignData(createdData.album), 'rgb(24,255,255)');
            drawChart('created-image-chart', '圖片建立 - Image Creation', alignData(createdData.image), 'rgb(255,204,0)');

            drawChart('used-short-url-chart', '短網址使用 - Short URL Usage', alignData(usedData.url), 'rgba(48,255,48)');
            drawChart('used-album-chart', '圖片相簿瀏覽 - Photo Album Browsing', alignData(usedData.album), 'rgba(24,255,255)');
            drawChart('used-image-chart', '圖片瀏覽 - Image Browsing', alignData(usedData.image), 'rgba(255,204,0)');
        })
        .catch(error => {
            console.error('Error fetching recent statistics:', error);

            const emptyData = initializeEmptyData();
            drawChart('created-short-url-chart', '短網址建立 - Short URL Creation', emptyData.url, 'rgba(75, 192, 75, 1)');
            drawChart('created-album-chart', '圖片相簿建立 - Image Album Creation', emptyData.album, 'rgba(54, 162, 235, 1)');
            drawChart('created-image-chart', '圖片建立 - Image Creation', emptyData.image, 'rgba(255, 206, 86, 1)');

            drawChart('used-short-url-chart', '短網址使用 - Short URL Usage', emptyData.url, 'rgba(75, 192, 75, 1)');
            drawChart('used-album-chart', '圖片相簿瀏覽 - Image Album Browsing', emptyData.album, 'rgba(54, 162, 235, 1)');
            drawChart('used-image-chart', '圖片瀏覽 - Image Browsing', emptyData.image, 'rgba(255, 206, 86, 1)');
        });

    function initializeEmptyData() {
        return Array(365).fill(0);
    }

    function alignData(data) {
        const aligned = Array(365).fill(null);
        const startIndex = 365 - data.length;
        data.forEach((value, index) => {
            aligned[startIndex + index] = value;
        });
        return aligned;
    }

    function drawChart(canvasId, title, dataset, color) {
        const canvas = document.getElementById(canvasId);
    
        // Apply canvas styles
        canvas.style.backgroundColor = 'rgb(29, 2, 29)';
        canvas.style.borderRadius = '5px';
        canvas.style.border = '3px solid #7a0a64';
        canvas.height = 350;

        const ctx = canvas.getContext('2d');
    
        const maxDataValue = Math.max(...dataset.filter(v => v !== null), 5);
        const suggestedMax = Math.max(maxDataValue + 25, 40);
    
        new Chart(ctx, {
            type: 'line',
            data: {
                labels: Array(365).fill(''),
                datasets: [
                    {
                        label: title,
                        data: dataset,
                        borderColor: color,
                        borderWidth: 1, 
                        pointBackgroundColor: color, 
                        pointRadius: 2,
                        pointHoverRadius: 3,
                        fill: false,
                        tension: 0.1,
                    },
                ],
            },
            options: {
                responsive: true,
                plugins: {
                    title: {
                        display: true,
                        text: title,
                        font: {
                            size: 16,
                            weight: 'bold',
                        },
                        color: 'white',
                    },
                    legend: {
                        display: false,
                    },
                    tooltip: {
                        callbacks: {
                            title: (tooltipItems) => {
                                const dayIndex = tooltipItems[0].dataIndex;
                                return `Past Day: ${365 - dayIndex}`;
                            },
                        },
                    },
                },
                scales: {
                    x: {
                        display: true,
                        grid: {
                            drawBorder: true, 
                            drawTicks: false, 
                            drawOnChartArea: false, 
                        },
                        border: {
                            color: 'white', 
                        },
                        title: {
                            display: true,
                            text: 'Time (days)', 
                            font: {
                                size: 14,
                                weight: 'bold',
                            },
                            color: 'white',
                        },
                        ticks: {
                            display: false, 
                        },
                    },
                    y: {
                        display: true,
                        grid: {
                            drawBorder: true,
                            drawTicks: false,
                            drawOnChartArea: false, 
                        },
                        border: {
                            color: 'white', 
                        },
                        beginAtZero: true,
                        suggestedMax: suggestedMax,
                        ticks: {
                            beginAtZero: true,
                            maxTicksLimit: 13,
                            callback: function (value) {
                                return Number.isInteger(value) ? value : '';
                            },
                            font: {
                                size: 12,
                                weight: 'bold',
                            },
                            color: 'white',
                        },
                        title: {
                            display: true,
                            text: 'Counts',
                            font: {
                                size: 14,
                                weight: 'bold',
                            },
                            color: 'white',
                        },
                    },
                },
            },
        });
    }    
});
