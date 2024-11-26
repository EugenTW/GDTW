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
            document.getElementById('total-short-urls-created').textContent = data.totalShortUrlsCreated || 0;
            document.getElementById('total-short-urls-used').textContent = data.totalShortUrlsUsed || 0;
            document.getElementById('total-image-albums-created').textContent = data.totalImageAlbumsCreated || 0;
            document.getElementById('total-image-albums-visited').textContent = data.totalImageAlbumsVisited || 0;
            document.getElementById('total-images-created').textContent = data.totalImagesCreated || 0;
            document.getElementById('total-images-visited').textContent = data.totalImagesVisited || 0;
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

            drawChart('created-short-url-chart', '短網址建立 - Short URL Creation', alignData(createdData.url), 'rgba(75, 192, 75, 1)');
            drawChart('created-album-chart', '圖片相簿建立 - Image Album Creation', alignData(createdData.album), 'rgba(54, 162, 235, 1)');
            drawChart('created-image-chart', '圖片建立 - Image Creation', alignData(createdData.image), 'rgba(255, 206, 86, 1)');

            drawChart('used-short-url-chart', '短網址使用 - Short URL Usage', alignData(usedData.url), 'rgba(75, 192, 75, 1)');
            drawChart('used-album-chart', '圖片相簿使用 - Image Album Usage', alignData(usedData.album), 'rgba(54, 162, 235, 1)');
            drawChart('used-image-chart', '圖片使用 - Image Usage', alignData(usedData.image), 'rgba(255, 206, 86, 1)');
        })
        .catch(error => {
            console.error('Error fetching recent statistics:', error);

            const emptyData = initializeEmptyData();
            drawChart('created-short-url-chart', '短網址建立 - Short URL Creation', emptyData.url, 'rgba(75, 192, 75, 1)');
            drawChart('created-album-chart', '圖片相簿建立 - Image Album Creation', emptyData.album, 'rgba(54, 162, 235, 1)');
            drawChart('created-image-chart', '圖片建立 - Image Creation', emptyData.image, 'rgba(255, 206, 86, 1)');

            drawChart('used-short-url-chart', '短網址使用 - Short URL Usage', emptyData.url, 'rgba(75, 192, 75, 1)');
            drawChart('used-album-chart', '圖片相簿使用 - Image Album Usage', emptyData.album, 'rgba(54, 162, 235, 1)');
            drawChart('used-image-chart', '圖片使用 - Image Usage', emptyData.image, 'rgba(255, 206, 86, 1)');
        });

    function initializeEmptyData() {
        return Array(360).fill(0);
    }

    function alignData(data) {
        const aligned = Array(360).fill(null);
        const startIndex = 360 - data.length;
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
        canvas.height = 100;
    
        const ctx = canvas.getContext('2d');
    
        const maxDataValue = Math.max(...dataset.filter(v => v !== null), 10);
        const suggestedMax = Math.max(maxDataValue + 5, 10);
    
        new Chart(ctx, {
            type: 'line',
            data: {
                labels: Array(360).fill(''),
                datasets: [
                    {
                        label: title,
                        data: dataset,
                        borderColor: color,
                        borderWidth: 1, 
                        pointBackgroundColor: color, 
                        pointRadius: 1.5, 
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
                                return `Past Day: ${360 - dayIndex}`;
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
                            stepSize: 1, 
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
