let charts = {};

const formatDate = (date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
};

function initDates() {
    const today = new Date();
    const dateStr = formatDate(today);
    document.getElementById('date-from').value = dateStr;
    document.getElementById('date-to').value = dateStr;
}

function setToday() {
    const today = formatDate(new Date());
    document.getElementById('date-from').value = today;
    document.getElementById('date-to').value = today;

    const granularity = document.getElementById('granularity').value;
    if (granularity !== 'period') {
        const picker = document.querySelector('#picker-wrapper input, #picker-wrapper select');
        if (picker) picker.value = '';
    }

    showRangeAndLoad();
}

function handleGranularityChange() {
    const granularity = document.getElementById('granularity').value;
    const wrapper = document.getElementById('picker-wrapper');
    const rangeGroup = document.getElementById('range-group');

    wrapper.innerHTML = '';
    rangeGroup.style.display = 'none'; // 단위 변경 시 범위 숨김

    const today = new Date();
    const year = today.getFullYear();

    if (granularity === 'week') {
        const input = document.createElement('input');
        input.type = 'date';
        input.id = 'week-picker';
        input.onchange = (e) => updateDatesByCustomWeek(e.target.value);
        wrapper.appendChild(input);
    } else if (granularity === 'month') {
        const input = document.createElement('input');
        input.type = 'month';
        input.id = 'month-picker';
        input.onchange = (e) => updateDatesByMonth(e.target.value);
        wrapper.appendChild(input);
    } else if (granularity === 'year') {
        const select = document.createElement('select');
        select.id = 'year-picker';
        const baseOpt = document.createElement('option');
        baseOpt.text = '연도 선택';
        baseOpt.value = '';
        select.appendChild(baseOpt);
        for (let y = year; y >= year - 5; y--) {
            const opt = document.createElement('option');
            opt.value = y;
            opt.text = `${y}년`;
            select.appendChild(opt);
        }
        select.onchange = (e) => {
            if (e.target.value) updateDatesByYear(e.target.value);
        };
        wrapper.appendChild(select);
    } else if (granularity === 'period') {
        showRangeAndLoad();
    }
}

function handleDateFromChange() {
    const from = document.getElementById('date-from').value;
    const toInput = document.getElementById('date-to');
    toInput.min = from;
    if (toInput.value && toInput.value < from) {
        toInput.value = from;
    }
    loadData();
}

function showRangeAndLoad() {
    document.getElementById('range-group').style.display = 'flex';
    loadData();
}

function updateDatesByCustomWeek(dateStr) {
    if (!dateStr) return;
    const [y, m, d] = dateStr.split('-').map(Number);
    const end = new Date(y, m - 1, d);
    const start = new Date(y, m - 1, d);
    start.setDate(end.getDate() - 6);

    document.getElementById('date-from').value = formatDate(start);
    document.getElementById('date-to').value = formatDate(end);
    showRangeAndLoad();
}

function updateDatesByMonth(monthStr) {
    if (!monthStr) return;
    const [year, month] = monthStr.split('-').map(Number);
    const firstDay = new Date(year, month - 1, 1);
    const lastDay = new Date(year, month, 0);

    document.getElementById('date-from').value = formatDate(firstDay);
    document.getElementById('date-to').value = formatDate(lastDay);
    showRangeAndLoad();
}

function updateDatesByYear(year) {
    if (!year) return;
    const firstDay = new Date(year, 0, 1);
    const lastDay = new Date(year, 11, 31);

    document.getElementById('date-from').value = formatDate(firstDay);
    document.getElementById('date-to').value = formatDate(lastDay);
    showRangeAndLoad();
}

async function initCompanies() {
    try {
        const response = await apiFetch('/api/analytics/companies');
        const result = await response.json();
        const companies = result.data;
        const select = document.getElementById('company-filter');
        companies.forEach(c => {
            const opt = document.createElement('option');
            opt.value = c.id;
            opt.text = `${c.name} (${c.businessNumber})`;
            select.appendChild(opt);
        });
    } catch (error) {
        console.error("Failed to load companies:", error);
    }
}

async function loadData() {
    const loading = document.getElementById('loading');
    loading.style.display = 'flex';

    const granularity = document.getElementById('granularity').value;
    const from = document.getElementById('date-from').value;
    const to = document.getElementById('date-to').value;
    const compare = document.getElementById('compare').value;
    const companyId = document.getElementById('company-filter').value;

    try {
        const response = await apiFetch(`/api/analytics/overview?granularity=${granularity}&from=${from}&to=${to}&compare=${compare}&companyId=${companyId}`);
        const result = await response.json();
        const data = result.data; // ResponseDto.data 추출

        updateKpis(data.kpis);
        renderCharts(data.series);

        const top10Section = document.getElementById('top10-section');
        if (companyId === "0") {
            top10Section.style.display = 'grid';
            renderTop10(data.top10);
        } else {
            top10Section.style.display = 'none';
        }
    } catch (error) {
        console.error('Data load failed:', error);
    } finally {
        loading.style.display = 'none';
    }
}

async function syncData() {
    const btn = document.getElementById('btn-sync');
    if (btn.classList.contains('spinning')) return;

    const result = await Swal.fire({
        title: '데이터 동기화',
        text: '어제까지의 원본 데이터를 집계 테이블에 반영하시겠습니까? (회사가 많은 경우 시간이 소요될 수 있습니다)',
        icon: 'question',
        showCancelButton: true,
        confirmButtonText: '진행',
        cancelButtonText: '취소',
        confirmButtonColor: '#3b82f6'
    });

    if (!result.isConfirmed) return;

    btn.classList.add('spinning');
    try {
        const response = await apiFetch('/api/analytics/sync', { method: 'POST' });
        if (response.ok) {
            await Swal.fire('완료', '데이터 집계가 성공적으로 완료되었습니다.', 'success');
            loadData();
        } else {
            throw new Error('Sync failed');
        }
    } catch (error) {
        Swal.fire('오류', '데이터 동기화 중 문제가 발생했습니다.', 'error');
    } finally {
        btn.classList.remove('spinning');
    }
}

function renderTop10(top10) {
    const renderTable = (id, list, formatFn) => {
        const body = document.getElementById(`top10-${id}-body`);
        body.innerHTML = list.map((item, idx) => `
        <tr style="border-bottom: 1px solid #f9fafb;">
            <td style="padding: 14px 12px; color: var(--primary); font-weight: 700;">${idx + 1}</td>
            <td style="font-weight: 500;">${item.name}</td>
            <td style="text-align: right; padding: 14px 12px; font-weight: 600;">${formatFn ? formatFn(item.value) : item.value.toLocaleString()}</td>
        </tr>
    `).join('');
        if (list.length === 0) body.innerHTML = '<tr><td colspan="3" style="text-align:center; padding: 40px; color: var(--text-muted);">데이터가 없습니다.</td></tr>';
    };

    renderTable('ai', top10.ai);
    renderTable('file', top10.file, (v) => (v / (1024 * 1024)).toFixed(1));
}

function updateKpis(kpis) {
    const companyId = document.getElementById('company-filter').value;
    const compareType = document.getElementById('compare').value;
    const labelEl = document.getElementById('kpi-company-label');

    if (companyId === "0") {
        labelEl.innerHTML = '전체 회사 <i class="fas fa-building"></i>';
    } else {
        labelEl.innerHTML = '선택 회사 <i class="fas fa-building"></i>';
    }

    const updateElement = (id, kpiKey, formatFn) => {
        const kpi = kpis[kpiKey];
        if (!kpi) return;

        const valEl = document.getElementById(`kpi-${id}`);
        const deltaEl = document.getElementById(`delta-${id}`);

        let val = parseFloat(kpi.current);
        if (id === 'storage') val = val / (1024 * 1024 * 1024); // To GB

        valEl.innerText = formatFn ? formatFn(val) : val.toLocaleString();

        if (compareType === 'none') {
            deltaEl.style.display = 'none';
        } else {
            deltaEl.style.display = 'inline-block';
            const delta = kpi.deltaPct;
            const deltaClass = delta >= 0 ? 'delta-up' : 'delta-down';
            const icon = delta >= 0 ? '<i class="fas fa-caret-up"></i>' : '<i class="fas fa-caret-down"></i>';
            const compareLabel = compareType === 'prev_year' ? 'vs 전년 동기' : 'vs 이전 기간';

            deltaEl.className = `kpi-delta ${deltaClass}`;
            deltaEl.innerHTML = `${icon} ${Math.abs(delta)}% (${compareLabel})`;
        }
    };

    updateElement('company', 'companyCount');
    updateElement('employee', 'employeeCount');
    updateElement('file', 'fileCount');
    updateElement('storage', 'fileBytes', (v) => v.toFixed(2));
    updateElement('ai', 'aiUsage');
}

function renderCharts(series) {
    const labels = series.map(s => s.label);

    // 1. Growth Chart (Company & Employee)
    updateChart('growthChart', 'line', {
        labels: labels,
        datasets: [
            {
                label: '회사 수',
                data: series.map(s => s.data.company_cnt),
                borderColor: '#3b82f6',
                backgroundColor: 'rgba(59, 130, 246, 0.1)',
                fill: true,
                borderWidth: 3,
                tension: 0.4,
                pointRadius: 4,
                pointBackgroundColor: '#fff'
            },
            {
                label: '평균 사원 수',
                data: series.map(s => s.data.emp_cnt),
                borderColor: '#10b981',
                backgroundColor: 'rgba(16, 185, 129, 0.1)',
                fill: true,
                borderWidth: 3,
                tension: 0.4,
                pointRadius: 4,
                pointBackgroundColor: '#fff'
            }
        ]
    }, {
        scales: {
            y: { beginAtZero: true, grid: { color: '#f3f4f6' }, ticks: { color: '#94a3b8' } },
            x: { offset: true, grid: { display: false }, ticks: { color: '#94a3b8' } }
        }
    });

    // 2. AI Distribution (Doughnut)
    const aiTotals = series.reduce((acc, curr) => {
        const d = curr.data;
        acc.schedule += (d.ai_schedule || 0);
        acc.doc += (d.ai_doc || 0);
        acc.compare += (d.ai_compare || 0);
        acc.outline += (d.ai_outline || 0);
        acc.chatbot += (d.ai_chatbot || 0);
        return acc;
    }, { schedule: 0, doc: 0, compare: 0, outline: 0, chatbot: 0 });

    updateChart('aiDistributionChart', 'doughnut', {
        labels: ['일정 요약', '문서 요약', '비교 요약', '구조 생성', '챗봇 매뉴얼'],
        datasets: [{
            data: [aiTotals.schedule, aiTotals.doc, aiTotals.compare, aiTotals.outline, aiTotals.chatbot],
            backgroundColor: ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6'],
            borderWidth: 4,
            borderColor: '#ffffff'
        }]
    }, {
        cutout: '75%',
        plugins: { legend: { position: 'bottom', labels: { color: '#64748b', padding: 20, font: { size: 12, weight: '600' } } } }
    });

    // 3. AI Trend
    updateChart('aiTrendChart', 'bar', {
        labels: labels,
        datasets: [{
            label: 'AI 총 요청 수',
            data: series.map(s => s.data.ai_total),
            backgroundColor: 'rgba(59, 130, 246, 0.8)',
            hoverBackgroundColor: '#2563eb',
            borderRadius: 8
        }]
    }, {
        scales: {
            y: { grid: { color: '#f3f4f6' }, ticks: { color: '#94a3b8' } },
            x: { offset: true, grid: { display: false }, ticks: { color: '#94a3b8' } }
        }
    });

    // 4. File Trend
    updateChart('fileTrendChart', 'line', {
        labels: labels,
        datasets: [{
            label: '파일 용량(MB)',
            data: series.map(s => s.data.file_bytes / (1024 * 1024)),
            borderColor: '#6366f1',
            backgroundColor: 'rgba(99, 102, 241, 0.1)',
            fill: true,
            borderWidth: 3,
            tension: 0.4,
            pointRadius: 4,
            pointBackgroundColor: '#fff'
        }]
    }, {
        scales: {
            y: {
                beginAtZero: true,
                grid: { color: '#f3f4f6' },
                ticks: {
                    color: '#94a3b8',
                    callback: function (value) {
                        return value.toLocaleString() + ' MB';
                    }
                }
            },
            x: { offset: true, grid: { display: false }, ticks: { color: '#94a3b8' } }
        }
    });
}

function updateChart(id, type, data, options = {}) {
    if (charts[id]) charts[id].destroy();

    const ctx = document.getElementById(id).getContext('2d');
    const commonOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
            legend: { labels: { color: '#64748b', font: { family: 'Outfit', weight: '600' } } }
        },
        ...options
    };

    charts[id] = new Chart(ctx, {
        type: type,
        data: data,
        options: commonOptions
    });
}

window.onload = async () => {
    // [보안] TEAM_ORBIT 권한 체크 (비인가 접근 차단)
    try {
        const response = await apiFetch('/api/auth/me');
        if (response.ok) {
            const meResult = await response.json();
            if (!meResult.data || meResult.data.role !== 'TEAM_ORBIT') {
                location.replace('/');
                return;
            }
        } else {
            location.replace('/login');
            return;
        }
    } catch (e) {
        console.error('Auth check error:', e);
        location.replace('/login');
        return;
    }

    initDates();
    handleGranularityChange(); // 초기 단위에 맞게 Picker 노출
    await initCompanies();
    loadData();
};
