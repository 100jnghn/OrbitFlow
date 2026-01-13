document.addEventListener('DOMContentLoaded', () => {
    let selectedCategoryId = null;
    let selectedFile = null;
    let categories = [];
    let editingCategoryId = null;

    // 카테고리 목록 로드
    async function loadCategories() {
        try {
            const token = sessionStorage.getItem('accessToken');
            if (!token) {
                sweetError('로그인이 필요합니다.');
                return;
            }

            const response = await fetch('/api/admin/manual/categories', {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                const result = await response.json();
                categories = result.data || [];

                if (categories.length === 0) {
                    const categoryButtonsContainer = document.getElementById('categoryButtons');
                    if (categoryButtonsContainer) {
                        categoryButtonsContainer.innerHTML = '<p style="color: #666; padding: 20px;">등록된 카테고리가 없습니다.</p>';
                    }
                    document.getElementById('selectedCategoryName').textContent = '카테고리 없음';
                    return;
                }

                renderCategoryButtons(categories);
                if (categories.length > 0) {
                    selectCategory(categories[0].id, categories[0].categoryName);
                }
            } else {
                const errorData = await response.json().catch(() => ({}));
                sweetError('카테고리 로드 실패: ' + (errorData.message || response.statusText));
            }
        } catch (error) {
            sweetError('카테고리 목록을 불러오는 중 오류가 발생했습니다.');
        }
    }

    function renderCategoryButtons(categoryList) {
        const categoryButtonsContainer = document.getElementById('categoryButtons');
        if (!categoryButtonsContainer) return;

        categoryButtonsContainer.innerHTML = '';
        categoryList.forEach(category => {
            const card = document.createElement('div');
            card.className = 'category-card';
            card.dataset.categoryId = category.id;

            const button = document.createElement('button');
            button.type = 'button';
            button.className = 'category-btn';
            button.textContent = category.categoryName || '이름 없음';
            button.addEventListener('click', () => selectCategory(category.id, category.categoryName));

            const deleteBtn = document.createElement('button');
            deleteBtn.type = 'button';
            deleteBtn.className = 'category-delete-btn';
            deleteBtn.innerHTML = '<i class="fas fa-times"></i>';
            deleteBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                deleteCategory(category.id);
            });

            card.appendChild(button);
            card.appendChild(deleteBtn);
            categoryButtonsContainer.appendChild(card);
        });
    }

    function selectCategory(categoryId, categoryName) {
        selectedCategoryId = categoryId;
        document.querySelectorAll('.category-btn').forEach(btn => btn.classList.remove('active'));
        const selectedCard = document.querySelector(`.category-card[data-category-id="${categoryId}"]`);
        if (selectedCard) {
            const selectedBtn = selectedCard.querySelector('.category-btn');
            if (selectedBtn) selectedBtn.classList.add('active');
        }
        document.getElementById('selectedCategoryName').textContent = categoryName || '선택 중...';
        loadManualList(categoryId);
    }

    document.getElementById('fileUploadArea').addEventListener('click', () => document.getElementById('fileInput').click());

    document.getElementById('fileInput').addEventListener('change', (e) => {
        const file = e.target.files[0];
        if (file) handleFileSelect(file);
    });

    const uploadArea = document.getElementById('fileUploadArea');
    uploadArea.addEventListener('dragover', (e) => { e.preventDefault(); uploadArea.classList.add('dragover'); });
    uploadArea.addEventListener('dragleave', () => uploadArea.classList.remove('dragover'));
    uploadArea.addEventListener('drop', (e) => {
        e.preventDefault();
        uploadArea.classList.remove('dragover');
        const file = e.dataTransfer.files[0];
        if (file) handleFileSelect(file);
    });

    function handleFileSelect(file) {
        const validExtensions = ['.txt', '.pdf', '.doc'];
        const fileExtension = file.name.substring(file.name.lastIndexOf('.')).toLowerCase();

        if (!validExtensions.includes(fileExtension)) {
            sweetWarning('TXT, PDF, DOC 파일만 업로드 가능합니다.');
            return;
        }

        if (file.size > 10 * 1024 * 1024) {
            sweetWarning('파일 크기는 10MB를 초과할 수 없습니다.');
            return;
        }

        selectedFile = file;
        const uploadContent = uploadArea.querySelector('.upload-content');
        uploadContent.innerHTML = `
            <i class="fas fa-file-alt upload-icon"></i>
            <p><strong>${file.name}</strong></p>
            <p class="upload-hint">${(file.size / 1024 / 1024).toFixed(2)} MB</p>
        `;
    }

    // 업로드 버튼 클릭
    document.getElementById('uploadBtn').addEventListener('click', async () => {
        if (!selectedFile) return sweetWarning('파일을 선택해주세요.');
        if (!selectedCategoryId) return sweetWarning('카테고리를 선택해주세요.');

        try {
            const formData = new FormData();
            formData.append('file', selectedFile);
            formData.append('categoryId', selectedCategoryId);

            const token = sessionStorage.getItem('accessToken');
            const response = await fetch('/api/admin/manual/upload', {
                method: 'POST',
                headers: { 'Authorization': `Bearer ${token}` },
                body: formData
            });

            if (response.ok) {
                await sweetSuccess('매뉴얼이 업로드되었습니다.');
                selectedFile = null;
                document.getElementById('fileInput').value = '';
                const uploadContent = uploadArea.querySelector('.upload-content');
                uploadContent.innerHTML = `
                    <i class="fas fa-cloud-upload-alt upload-icon"></i>
                    <p>파일을 드래그하여 여기에 놓거나, 클릭하여 파일을 선택하세요.</p>
                    <p class="upload-hint">최대 10MB (TXT, PDF, DOC)</p>
                `;
                loadManualList(selectedCategoryId);
            } else {
                const error = await response.json();
                sweetError('업로드 실패: ' + (error.message || '알 수 없는 오류'));
            }
        } catch (error) {
            sweetError('업로드 중 오류가 발생했습니다.');
        }
    });

    async function loadManualList(categoryId) {
        try {
            const token = sessionStorage.getItem('accessToken');
            const url = categoryId ? `/api/admin/manual/list?categoryId=${categoryId}` : '/api/admin/manual/list';
            const response = await fetch(url, { headers: { 'Authorization': `Bearer ${token}` } });

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
                        const registeredDate = manual.createdAt ? new Date(manual.createdAt).toLocaleDateString('ko-KR') : '-';
                        row.innerHTML = `
                            <td>${manual.fileName || '알 수 없음'}</td>
                            <td>${registeredDate}</td>
                            <td><button class="btn-delete" data-id="${manual.id}">삭제</button></td>
                        `;
                        tbody.appendChild(row);
                    });

                    document.querySelectorAll('.btn-delete').forEach(btn => {
                        btn.addEventListener('click', () => deleteManual(btn.dataset.id));
                    });
                }
            }
        } catch (error) {
            sweetError('목록 로드 중 오류가 발생했습니다.');
        }
    }

    async function deleteManual(manualId) {
        const confirm = await sweetConfirm('매뉴얼 삭제', '정말 이 매뉴얼을 삭제하시겠습니까?');
        if (!confirm.isConfirmed) return;

        try {
            const token = sessionStorage.getItem('accessToken');
            const response = await fetch(`/api/admin/manual/${manualId}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                await sweetSuccess('매뉴얼이 삭제되었습니다.');
                loadManualList(selectedCategoryId);
            } else {
                const error = await response.json();
                sweetError('삭제 실패: ' + (error.message || '알 수 없는 오류'));
            }
        } catch (error) {
            sweetError('삭제 중 오류가 발생했습니다.');
        }
    }

    document.getElementById('addCategoryBtn').addEventListener('click', () => openCategoryModal(null));

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

    window.closeCategoryModal = function() {
        document.getElementById('categoryModal').style.display = 'none';
        editingCategoryId = null;
        document.getElementById('categoryForm').reset();
    };

    window.saveCategory = async function() {
        const categoryName = document.getElementById('categoryName').value.trim();
        const description = document.getElementById('categoryDescription').value.trim();
        const sortOrder = document.getElementById('categorySortOrder').value;

        if (!categoryName) return sweetWarning('카테고리 이름을 입력해주세요.');

        try {
            const token = sessionStorage.getItem('accessToken');
            const url = editingCategoryId ? `/api/admin/manual/categories/${editingCategoryId}` : '/api/admin/manual/categories';
            const method = editingCategoryId ? 'PUT' : 'POST';

            const response = await fetch(url, {
                method: method,
                headers: { 'Authorization': `Bearer ${token}`, 'Content-Type': 'application/json' },
                body: JSON.stringify({ categoryName, description: description || null, sortOrder: sortOrder ? parseInt(sortOrder) : null })
            });

            if (response.ok) {
                await sweetSuccess(editingCategoryId ? '카테고리가 수정되었습니다.' : '카테고리가 추가되었습니다.');
                closeCategoryModal();
                await loadCategories();
            } else {
                const error = await response.json();
                sweetError('저장 실패: ' + (error.message || '알 수 없는 오류'));
            }
        } catch (error) {
            sweetError('저장 중 오류가 발생했습니다.');
        }
    };

    async function deleteCategory(categoryId) {
        const confirm = await sweetConfirm('카테고리 삭제', '카테고리를 삭제하시겠습니까?\n속한 매뉴얼도 함께 삭제될 수 있습니다.');
        if (!confirm.isConfirmed) return;

        try {
            const token = sessionStorage.getItem('accessToken');
            const response = await fetch(`/api/admin/manual/categories/${categoryId}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (response.ok) {
                await sweetSuccess('카테고리가 삭제되었습니다.');
                if (selectedCategoryId === categoryId) {
                    selectedCategoryId = null;
                    document.getElementById('selectedCategoryName').textContent = '선택 중...';
                    document.getElementById('manualListBody').innerHTML = '<tr><td colspan="3" style="text-align: center; padding: 20px;">카테고리를 선택해주세요.</td></tr>';
                }
                await loadCategories();
            } else {
                const error = await response.json();
                sweetError('삭제 실패: ' + (error.message || '알 수 없는 오류'));
            }
        } catch (error) {
            sweetError('삭제 중 오류가 발생했습니다.');
        }
    }

    document.getElementById('categoryModal').addEventListener('click', (e) => {
        if (e.target.id === 'categoryModal') closeCategoryModal();
    });

    loadCategories();
});