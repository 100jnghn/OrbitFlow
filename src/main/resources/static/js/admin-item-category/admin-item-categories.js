/**
 * 관리자 - 카테고리 관리 페이지
 */

/* ==========================
   Validation
========================== */
function validateCategoryName(input, msgElement) {
    const value = input.value.trim();
    
    if (!value) {
        input.classList.add('error');
        input.classList.remove('success');
        if (msgElement) {
            msgElement.textContent = '카테고리명을 입력해주세요. (0/50)';
            msgElement.className = 'validation-msg error';
        }
        return false;
    }
    
    input.classList.remove('error');
    input.classList.add('success');
    if (msgElement) {
        msgElement.textContent = `입력됨 (${value.length}/50)`;
        msgElement.className = 'validation-msg success';
    }
    return true;
}

function attachValidationToInput(input, button, container) {
    // validation 메시지 요소 생성 (container에 추가)
    const msgElement = document.createElement('span');
    msgElement.className = 'validation-msg';
    container.appendChild(msgElement);
    
    // 실시간 검증
    input.addEventListener('input', () => {
        const isValid = validateCategoryName(input, msgElement);
        if (button) {
            button.disabled = !isValid;
        }
    });
    
    // 초기 검증
    validateCategoryName(input, msgElement);
}

function attachValidationToInputForEdit(input, button, container) {
    // validation 메시지 요소 생성 (container에 추가)
    const msgElement = document.createElement('span');
    msgElement.className = 'validation-msg';
    container.appendChild(msgElement);
    
    // 실시간 검증
    input.addEventListener('input', () => {
        const isValid = validateCategoryName(input, msgElement);
        const originalValue = input.dataset.originalValue || '';
        const currentValue = input.value.trim();
        const isChanged = currentValue !== originalValue;
        
        // validation이 통과하고 값이 변경된 경우에만 버튼 활성화
        if (button) {
            button.disabled = !(isValid && isChanged);
        }
    });
    
    // 초기 검증 및 버튼 상태 설정
    validateCategoryName(input, msgElement);
    if (button) {
        button.disabled = true; // 초기에는 비활성화
    }
}

/* ==========================
   Data Load
========================== */
async function loadCategories() {
    try {
        const res = await apiFetch(
            '/api/item-categories',
            { method: 'GET' }
        );

        if (!res.ok) throw new Error();

        const { data } = await res.json();
        const tbody = document.querySelector('.category-table tbody');
        tbody.innerHTML = '';

        if (!data?.length) {
            // 빈 입력 행만 추가
            tbody.appendChild(createNewCategoryRow(1));
            return;
        }

        data.forEach((category, i) => {
            const tr = document.createElement('tr');
            tr.append(
                createCell(i + 1),
                createCategoryCell(category.id, category.name)
            );
            tbody.appendChild(tr);
        });

        // 마지막에 빈 입력 행 추가
        tbody.appendChild(createNewCategoryRow(data.length + 1));

    } catch (e) {
        console.error(e);
        alert('카테고리 목록을 불러오지 못했습니다.');
    }
}

/* ==========================
   Table Cell Helpers
========================== */
function createCell(value) {
    const td = document.createElement('td');
    td.textContent = value ?? '';
    return td;
}

function createCategoryCell(id, value) {
    const td = document.createElement('td');
    
    // 수평 정렬을 위한 컨테이너
    const rowContainer = document.createElement('div');
    rowContainer.className = 'category-row';
    
    // 입력 필드
    const input = document.createElement('input');
    input.type = 'text';
    input.className = 'category-name-input';
    input.value = value ?? '';
    input.maxLength = 50;
    input.dataset.categoryId = id;
    input.dataset.originalValue = value ?? ''; // 초기값 저장
    
    // 버튼 그룹
    const actionBox = document.createElement('div');
    actionBox.className = 'action-btns';

    const edit = document.createElement('button');
    edit.className = 'btn-edit';
    edit.textContent = '수정';
    edit.onclick = () => updateCategory(id);

    const del = document.createElement('button');
    del.className = 'btn-delete';
    del.textContent = '삭제';
    del.onclick = () => deleteCategory(id);

    actionBox.append(edit, del);
    rowContainer.append(input, actionBox);
    td.appendChild(rowContainer);
    
    // validation 추가
    setTimeout(() => {
        attachValidationToInputForEdit(input, edit, td);
    }, 0);
    
    return td;
}

function createNewCategoryRow(rowNumber) {
    const tr = document.createElement('tr');
    tr.className = 'new-category-row';

    // 번호
    const numberCell = createCell(rowNumber);

    // 카테고리 셀
    const categoryCell = document.createElement('td');
    
    // 수평 정렬을 위한 컨테이너
    const rowContainer = document.createElement('div');
    rowContainer.className = 'category-row';
    
    // 입력 필드
    const input = document.createElement('input');
    input.type = 'text';
    input.className = 'category-name-input';
    input.id = 'new-category-input';
    input.placeholder = '새 카테고리 입력';
    input.maxLength = 50;
    
    // 추가 버튼
    const addBtn = document.createElement('button');
    addBtn.className = 'btn-add-row';
    addBtn.innerHTML = '<i class="fas fa-plus"></i> 카테고리 추가';
    addBtn.onclick = () => addNewCategory();
    addBtn.disabled = true; // 초기에는 비활성화
    
    // Enter 키로 추가
    input.addEventListener('keypress', (e) => {
        if (e.key === 'Enter' && !addBtn.disabled) {
            addNewCategory();
        }
    });
    
    rowContainer.append(input, addBtn);
    categoryCell.appendChild(rowContainer);

    tr.append(numberCell, categoryCell);
    
    // validation 추가
    attachValidationToInput(input, addBtn, categoryCell);
    
    return tr;
}

/* ==========================
   CRUD Operations
========================== */
async function addNewCategory() {
    const input = document.getElementById('new-category-input');
    const msgElement = input.closest('td').querySelector('.validation-msg');
    
    // validation 확인
    if (!validateCategoryName(input, msgElement)) {
        alert('카테고리명을 입력해주세요.');
        input.focus();
        return;
    }
    
    const name = input.value.trim();

    try {
        const res = await apiFetch(
            '/api/admin/item-categories',
            {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name })
            }
        );

        if (!res.ok) throw new Error();

        alert('카테고리가 추가되었습니다.');
        loadCategories();

    } catch (e) {
        console.error(e);
        alert('카테고리 추가에 실패했습니다.');
    }
}

async function updateCategory(id) {
    const input = document.querySelector(`input[data-category-id="${id}"]`);
    const msgElement = input.closest('td').querySelector('.validation-msg');
    
    // validation 확인
    if (!validateCategoryName(input, msgElement)) {
        alert('카테고리명을 입력해주세요.');
        input.focus();
        return;
    }
    
    const name = input.value.trim();

    try {
        const res = await apiFetch(
            `/api/admin/item-categories/${id}`,
            {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name })
            }
        );

        if (!res.ok) throw new Error();

        alert('카테고리가 수정되었습니다.');
        loadCategories();

    } catch (e) {
        console.error(e);
        alert('카테고리 수정에 실패했습니다.');
    }
}

async function deleteCategory(id) {
    if (!confirm('카테고리를 삭제하시겠습니까?\n카테고리에 속한 비품이 같이 삭제됩니다.')) {
        return;
    }

    try {
        const res = await apiFetch(
            `/api/admin/item-categories/${id}`,
            { method: 'DELETE' }
        );

        if (!res.ok) {
            const error = await res.json();
            throw new Error(error.message || '삭제 실패');
        }

        alert('카테고리가 삭제되었습니다.');
        loadCategories();

    } catch (e) {
        console.error(e);
        if (e.message.includes('비품')) {
            alert('카테고리에 속한 비품이 있어 삭제할 수 없습니다.');
        } else {
            alert('카테고리 삭제에 실패했습니다.');
        }
    }
}

/* ==========================
   초기화
========================== */
document.addEventListener('DOMContentLoaded', () => {
    loadCategories();
});

