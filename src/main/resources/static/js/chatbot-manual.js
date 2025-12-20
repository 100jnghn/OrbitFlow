document.addEventListener('DOMContentLoaded', () => {
    let selectedCategory = 'attendance';
    let selectedFile = null;

    // 카테고리 버튼 클릭 이벤트
    document.querySelectorAll('.category-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            // 모든 버튼에서 active 클래스 제거
            document.querySelectorAll('.category-btn').forEach(b => b.classList.remove('active'));
            // 클릭한 버튼에 active 클래스 추가
            btn.classList.add('active');
            selectedCategory = btn.dataset.category;
            
            // 카테고리 이름 업데이트
            const categoryNames = {
                'attendance': '근태',
                'approval': '결재',
                'resource': '자원관리',
                'schedule': '일정',
                'other': '기타'
            };
            document.getElementById('selectedCategoryName').textContent = categoryNames[selectedCategory] || '근태';
            
            // 해당 카테고리의 매뉴얼 목록 로드
            loadManualList(selectedCategory);
        });
    });

    // 첫 번째 카테고리(근태)를 기본 선택
    document.querySelector('.category-btn[data-category="attendance"]').classList.add('active');

    // 파일 업로드 영역 클릭
    document.getElementById('fileUploadArea').addEventListener('click', () => {
        document.getElementById('fileInput').click();
    });

    // 파일 선택
    document.getElementById('fileInput').addEventListener('change', (e) => {
        const file = e.target.files[0];
        if (file) {
            handleFileSelect(file);
        }
    });

    // 드래그 앤 드롭
    const uploadArea = document.getElementById('fileUploadArea');
    
    uploadArea.addEventListener('dragover', (e) => {
        e.preventDefault();
        uploadArea.classList.add('dragover');
    });

    uploadArea.addEventListener('dragleave', () => {
        uploadArea.classList.remove('dragover');
    });

    uploadArea.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadArea.classList.remove('dragover');
        
        const file = e.dataTransfer.files[0];
        if (file) {
            handleFileSelect(file);
        }
    });

    // 파일 선택 처리
    function handleFileSelect(file) {
        // 파일 확장자 확인
        if (!file.name.endsWith('.txt')) {
            alert('TXT 파일만 업로드 가능합니다.');
            return;
        }

        // 파일 크기 확인 (10MB)
        if (file.size > 10 * 1024 * 1024) {
            alert('파일 크기는 10MB를 초과할 수 없습니다.');
            return;
        }

        selectedFile = file;
        
        // 업로드 영역에 파일명 표시
        const uploadContent = uploadArea.querySelector('.upload-content');
        uploadContent.innerHTML = `
            <i class="fas fa-file-alt upload-icon"></i>
            <p><strong>${file.name}</strong></p>
            <p class="upload-hint">${(file.size / 1024 / 1024).toFixed(2)} MB</p>
        `;
    }

    // 업로드 버튼 클릭
    document.getElementById('uploadBtn').addEventListener('click', async () => {
        if (!selectedFile) {
            alert('파일을 선택해주세요.');
            return;
        }

        if (!selectedCategory) {
            alert('카테고리를 선택해주세요.');
            return;
        }

        try {
            const formData = new FormData();
            formData.append('file', selectedFile);
            formData.append('category', selectedCategory);

            const token = sessionStorage.getItem('accessToken');
            const response = await fetch('/api/admin/chatbot/manual/upload', {
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                body: formData
            });

            if (response.ok) {
                alert('매뉴얼이 업로드되었습니다.');
                selectedFile = null;
                document.getElementById('fileInput').value = '';
                // 업로드 영역 초기화
                const uploadContent = uploadArea.querySelector('.upload-content');
                uploadContent.innerHTML = `
                    <i class="fas fa-cloud-upload-alt upload-icon"></i>
                    <p>파일을 드래그하여 여기에 놓거나, 클릭하여 파일을 선택하세요.</p>
                    <p class="upload-hint">최대 10MB (TXT)</p>
                `;
                loadManualList(selectedCategory);
            } else {
                const error = await response.json();
                alert('업로드 실패: ' + (error.message || '알 수 없는 오류'));
            }
        } catch (error) {
            console.error('Upload error:', error);
            alert('업로드 중 오류가 발생했습니다.');
        }
    });

    // 매뉴얼 목록 로드
    async function loadManualList(category) {
        try {
            const token = sessionStorage.getItem('accessToken');
            const response = await fetch(`/api/admin/chatbot/manual?category=${category}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            const tbody = document.getElementById('manualListBody');
            tbody.innerHTML = '';

            if (response.ok) {
                const data = await response.json();
                const manuals = data.data || [];

                if (manuals.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="3" style="text-align: center; padding: 20px;">등록된 매뉴얼이 없습니다.</td></tr>';
                } else {
                    manuals.forEach(manual => {
                        const row = document.createElement('tr');
                        const registeredDate = manual.registeredDate ? 
                            new Date(manual.registeredDate).toLocaleDateString('ko-KR') : '-';
                        
                        row.innerHTML = `
                            <td>${manual.fileName || '알 수 없음'}</td>
                            <td>${registeredDate}</td>
                            <td>
                                <button class="btn-delete" data-id="${manual.id}">삭제</button>
                            </td>
                        `;
                        tbody.appendChild(row);
                    });

                    // 삭제 버튼 이벤트
                    document.querySelectorAll('.btn-delete').forEach(btn => {
                        btn.addEventListener('click', () => deleteManual(btn.dataset.id));
                    });
                }
            } else {
                tbody.innerHTML = '<tr><td colspan="3" style="text-align: center; padding: 20px;">목록을 불러오는데 실패했습니다.</td></tr>';
            }
        } catch (error) {
            console.error('Load manual list error:', error);
            const tbody = document.getElementById('manualListBody');
            tbody.innerHTML = '<tr><td colspan="3" style="text-align: center; padding: 20px;">목록을 불러오는 중 오류가 발생했습니다.</td></tr>';
        }
    }

    // 매뉴얼 삭제
    async function deleteManual(manualId) {
        if (!confirm('정말 삭제하시겠습니까?')) {
            return;
        }

        try {
            const token = sessionStorage.getItem('accessToken');
            const response = await fetch(`/api/admin/chatbot/manual/${manualId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.ok) {
                alert('매뉴얼이 삭제되었습니다.');
                loadManualList(selectedCategory);
            } else {
                const error = await response.json();
                alert('삭제 실패: ' + (error.message || '알 수 없는 오류'));
            }
        } catch (error) {
            console.error('Delete error:', error);
            alert('삭제 중 오류가 발생했습니다.');
        }
    }

    // 초기 매뉴얼 목록 로드
    loadManualList(selectedCategory);
});

