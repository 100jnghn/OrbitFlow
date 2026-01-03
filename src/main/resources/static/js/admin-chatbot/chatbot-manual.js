document.addEventListener('DOMContentLoaded', () => {
    let selectedCategoryId = null;
    let selectedFile = null;
    let categories = [];
    let editingCategoryId = null; // 수정 중인 카테고리 ID

    // 카테고리 목록 로드
    async function loadCategories() {
        try {
            const token = sessionStorage.getItem('accessToken');
            if (!token) {
                console.error('토큰이 없습니다.');
                alert('로그인이 필요합니다.');
                return;
            }

            const response = await fetch('/api/admin/manual/categories', {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.ok) {
                const result = await response.json();
                console.log('카테고리 API 응답:', result);
                
                // ResponseDto 구조 확인
                categories = result.data || [];
                console.log('카테고리 목록:', categories);
                
                if (categories.length === 0) {
                    // 카테고리가 없을 때 메시지 표시
                    const categoryButtonsContainer = document.getElementById('categoryButtons');
                    if (categoryButtonsContainer) {
                        categoryButtonsContainer.innerHTML = '<p style="color: #666; padding: 20px;">등록된 카테고리가 없습니다.</p>';
                    }
                    document.getElementById('selectedCategoryName').textContent = '카테고리 없음';
                    return;
                }
                
                // 카테고리 버튼 생성
                renderCategoryButtons(categories);
                
                // 첫 번째 카테고리를 기본 선택
                if (categories.length > 0) {
                    selectCategory(categories[0].id, categories[0].categoryName);
                }
            } else {
                const errorData = await response.json().catch(() => ({}));
                console.error('카테고리 목록 로드 실패:', response.status, errorData);
                alert('카테고리 목록을 불러오는데 실패했습니다: ' + (errorData.message || response.statusText));
            }
        } catch (error) {
            console.error('Load categories error:', error);
            alert('카테고리 목록을 불러오는 중 오류가 발생했습니다: ' + error.message);
        }
    }

    // 카테고리 버튼 렌더링
    function renderCategoryButtons(categoryList) {
        const categoryButtonsContainer = document.getElementById('categoryButtons');
        if (!categoryButtonsContainer) {
            console.error('categoryButtons 컨테이너를 찾을 수 없습니다.');
            return;
        }

        categoryButtonsContainer.innerHTML = '';

        if (!categoryList || categoryList.length === 0) {
            categoryButtonsContainer.innerHTML = '<p style="color: #666; padding: 20px;">등록된 카테고리가 없습니다.</p>';
            return;
        }

        categoryList.forEach(category => {
            const wrapper = document.createElement('div');
            wrapper.className = 'category-btn-wrapper';
            
            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'category-btn';
            button.dataset.categoryId = category.id;
            button.textContent = category.categoryName || '이름 없음';
            
            button.addEventListener('click', () => {
                selectCategory(category.id, category.categoryName);
            });

            const actions = document.createElement('div');
            actions.className = 'category-btn-actions';
            
            const editBtn = document.createElement('button');
            editBtn.type = 'button';
            editBtn.className = 'btn-edit-category';
            editBtn.innerHTML = '<i class="fas fa-edit"></i>';
            editBtn.title = '수정';
            editBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                openCategoryModal(category);
            });
            
            const deleteBtn = document.createElement('button');
            deleteBtn.type = 'button';
            deleteBtn.className = 'btn-delete-category';
            deleteBtn.innerHTML = '<i class="fas fa-trash"></i>';
            deleteBtn.title = '삭제';
            deleteBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                deleteCategory(category.id);
            });
            
            actions.appendChild(editBtn);
            actions.appendChild(deleteBtn);
            
            wrapper.appendChild(button);
            wrapper.appendChild(actions);
            categoryButtonsContainer.appendChild(wrapper);
        });
    }

    // 카테고리 선택
    function selectCategory(categoryId, categoryName) {
        selectedCategoryId = categoryId;
        
            // 모든 버튼에서 active 클래스 제거
        document.querySelectorAll('.category-btn').forEach(btn => btn.classList.remove('active'));
        
        // 선택한 버튼에 active 클래스 추가
        const selectedBtn = document.querySelector(`.category-btn[data-category-id="${categoryId}"]`);
        if (selectedBtn) {
            selectedBtn.classList.add('active');
        }
        
        // 카테고리 이름 업데이트
        document.getElementById('selectedCategoryName').textContent = categoryName || '선택 중...';
            
            // 해당 카테고리의 매뉴얼 목록 로드
        loadManualList(categoryId);
    }

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
        // 파일 확장자 확인 (백엔드가 PDF를 받지만, 프론트엔드에서 TXT도 허용하도록 함)
        const validExtensions = ['.txt', '.pdf'];
        const fileExtension = file.name.substring(file.name.lastIndexOf('.')).toLowerCase();
        
        if (!validExtensions.includes(fileExtension)) {
            alert('TXT 또는 PDF 파일만 업로드 가능합니다.');
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

        if (!selectedCategoryId) {
            alert('카테고리를 선택해주세요.');
            return;
        }

        try {
            const formData = new FormData();
            formData.append('file', selectedFile);
            formData.append('categoryId', selectedCategoryId);

            const token = sessionStorage.getItem('accessToken');
            const response = await fetch('/api/admin/manual/upload', {
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
                    <p class="upload-hint">최대 10MB (TXT, PDF)</p>
                `;
                loadManualList(selectedCategoryId);
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
    async function loadManualList(categoryId) {
        try {
            const token = sessionStorage.getItem('accessToken');
            const url = categoryId 
                ? `/api/admin/manual/list?categoryId=${categoryId}`
                : '/api/admin/manual/list';
            
            const response = await fetch(url, {
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
                        // createdAt 필드를 사용하여 날짜 표시
                        const registeredDate = manual.createdAt 
                            ? new Date(manual.createdAt).toLocaleDateString('ko-KR') 
                            : '-';
                        
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
            const response = await fetch(`/api/admin/manual/${manualId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });

            if (response.ok) {
                alert('매뉴얼이 삭제되었습니다.');
                loadManualList(selectedCategoryId);
            } else {
                const error = await response.json();
                alert('삭제 실패: ' + (error.message || '알 수 없는 오류'));
            }
        } catch (error) {
            console.error('Delete error:', error);
            alert('삭제 중 오류가 발생했습니다.');
        }
    }

    // 카테고리 추가 버튼 클릭
    document.getElementById('addCategoryBtn').addEventListener('click', () => {
        openCategoryModal(null);
    });

    // 카테고리 모달 열기
    function openCategoryModal(category) {
        editingCategoryId = category ? category.id : null;
        const modal = document.getElementById('categoryModal');
        const modalTitle = document.getElementById('modalTitle');
        const form = document.getElementById('categoryForm');
        
        if (category) {
            modalTitle.textContent = '카테고리 수정';
            document.getElementById('categoryName').value = category.categoryName || '';
            document.getElementById('categoryDescription').value = category.description || '';
            document.getElementById('categorySortOrder').value = category.sortOrder || '';
        } else {
            modalTitle.textContent = '카테고리 추가';
            form.reset();
        }
        
        modal.style.display = 'flex';
    }

    // 카테고리 모달 닫기
    window.closeCategoryModal = function() {
        const modal = document.getElementById('categoryModal');
        modal.style.display = 'none';
        editingCategoryId = null;
        document.getElementById('categoryForm').reset();
    };

    // 카테고리 저장 (추가/수정)
    window.saveCategory = async function() {
        const categoryName = document.getElementById('categoryName').value.trim();
        const description = document.getElementById('categoryDescription').value.trim();
        const sortOrder = document.getElementById('categorySortOrder').value;
        
        if (!categoryName) {
            alert('카테고리 이름을 입력해주세요.');
            return;
        }
        
        try {
            const token = sessionStorage.getItem('accessToken');
            const url = editingCategoryId 
                ? `/api/admin/manual/categories/${editingCategoryId}`
                : '/api/admin/manual/categories';
            
            const method = editingCategoryId ? 'PUT' : 'POST';
            
            const requestBody = {
                categoryName: categoryName,
                description: description || null,
                sortOrder: sortOrder ? parseInt(sortOrder) : null
            };
            
            const response = await fetch(url, {
                method: method,
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            });
            
            if (response.ok) {
                const result = await response.json();
                alert(editingCategoryId ? '카테고리가 수정되었습니다.' : '카테고리가 추가되었습니다.');
                closeCategoryModal();
                await loadCategories(); // 카테고리 목록 새로고침
            } else {
                const error = await response.json();
                alert('저장 실패: ' + (error.message || '알 수 없는 오류'));
            }
        } catch (error) {
            console.error('Save category error:', error);
            alert('저장 중 오류가 발생했습니다.');
        }
    };

    // 카테고리 삭제
    async function deleteCategory(categoryId) {
        if (!confirm('정말 이 카테고리를 삭제하시겠습니까?\n카테고리에 속한 매뉴얼도 함께 삭제될 수 있습니다.')) {
            return;
        }
        
        try {
            const token = sessionStorage.getItem('accessToken');
            const response = await fetch(`/api/admin/manual/categories/${categoryId}`, {
                method: 'DELETE',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (response.ok) {
                alert('카테고리가 삭제되었습니다.');
                // 삭제된 카테고리가 선택된 카테고리인 경우 선택 해제
                if (selectedCategoryId === categoryId) {
                    selectedCategoryId = null;
                    document.getElementById('selectedCategoryName').textContent = '선택 중...';
                    const tbody = document.getElementById('manualListBody');
                    tbody.innerHTML = '<tr><td colspan="3" style="text-align: center; padding: 20px;">카테고리를 선택해주세요.</td></tr>';
                }
                await loadCategories(); // 카테고리 목록 새로고침
            } else {
                const error = await response.json();
                alert('삭제 실패: ' + (error.message || '알 수 없는 오류'));
            }
        } catch (error) {
            console.error('Delete category error:', error);
            alert('삭제 중 오류가 발생했습니다.');
        }
    }

    // 모달 외부 클릭 시 닫기
    document.getElementById('categoryModal').addEventListener('click', (e) => {
        if (e.target.id === 'categoryModal') {
            closeCategoryModal();
        }
    });

    // 초기화: 카테고리 목록 로드
    loadCategories();
});
