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

document.addEventListener('DOMContentLoaded', function () {
    const apiEndpoint = '/ds_api/recent_statistics';

    fetch(apiEndpoint, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({}),
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP 錯誤！狀態碼：${response.status}`);
            }
            return response.json();
        })
        .then(responseData => {
            const data = responseData.data;

            const createdData = data.created || initializeEmptyData();
            const usedData = data.used || initializeEmptyData();

            drawChart('created-chart', '', createdData);
            drawChart('used-chart', '', usedData);
        })
        .catch(error => {
            console.error('獲取數據時出錯：', error);

            const emptyData = initializeEmptyData();
            drawChart('created-chart', '', emptyData);
            drawChart('used-chart', '', emptyData);
        });

    function initializeEmptyData() {
        const emptyArray = Array(365).fill(0);
        return {
            url: emptyArray,
            album: emptyArray,
            image: emptyArray,
        };
    }

    function drawChart(canvasId, title, data) {
        const ctx = document.getElementById(canvasId).getContext('2d');

        const maxDataValue = Math.max(
            ...data.url,
            ...data.album,
            ...data.image,
            10
        );

        const labels = generateDateLabels(data.url.length);

        new Chart(ctx, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Short URL',
                        data: data.url,
                        borderColor: 'rgba(255, 99, 132, 1)',
                        backgroundColor: 'rgba(255, 99, 132, 0.2)',
                        fill: false,
                        tension: 0.1,
                    },
                    {
                        label: 'Image Album',
                        data: data.album,
                        borderColor: 'rgba(54, 162, 235, 1)',
                        backgroundColor: 'rgba(54, 162, 235, 0.2)',
                        fill: false,
                        tension: 0.1,
                    },
                    {
                        label: 'Image',
                        data: data.image,
                        borderColor: 'rgba(255, 206, 86, 1)',
                        backgroundColor: 'rgba(255, 206, 86, 0.2)',
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
                        position: 'top',
                        labels: {
                            font: {
                                size: 12,
                                weight: 'bold',
                            },
                            color: 'white', 
                        },
                    },
                },
                scales: {
                    x: {
                        title: {
                            display: true,
                            text: '日期 - Date',
                            font: {
                                size: 14,
                                weight: 'bold',
                            },
                            color: 'white', 
                        },
                        ticks: {
                            autoSkip: true,
                            maxTicksLimit: 12,
                            padding: 10,
                            maxRotation: 90,
                            minRotation: 90,
                            font: {
                                size: 12,
                                weight: 'bold',
                            },
                            color: 'white', 
                        },
                    },
                    y: {
                        beginAtZero: true,
                        suggestedMax: maxDataValue + 10,
                        title: {
                            display: true,
                            text: '次數 - Times',
                            font: {
                                size: 14,
                                weight: 'bold',
                            },
                            color: 'white', 
                        },
                        ticks: {
                            font: {
                                size: 12,
                                weight: 'bold',
                            },
                            color: 'white', 
                        },
                    },
                },
            },
        });
    }

    function generateDateLabels(totalDays) {
        const today = new Date();
        const labels = [];
        for (let i = 0; i < totalDays; i++) {
            const date = new Date(today);
            date.setDate(today.getDate() - i);
            if (i % 30 === 0 || i === totalDays - 1) {
                labels.unshift(date.toISOString().slice(0, 10));
            } else {
                labels.unshift('');
            }
        }
        return labels;
    }
});








