package com.finalproj.orbitflow.board.service.admin;

import com.finalproj.orbitflow.board.dto.admin.AdminBoardResDto;
import com.finalproj.orbitflow.board.entity.BoardCategory;
import com.finalproj.orbitflow.board.repository.BoardCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminBoardCategoryService  {

    private final BoardCategoryRepository boardCategoryRepository;

    /**
     * 관리자 게시판(카테고리) 목록 조회
     * - 삭제되지 않은 카테고리만 조회
     * - 활성/비활성 여부 상관없이 조회
     */
    public Page<AdminBoardResDto.Category> getCategoryList(Long companyId, Pageable pageable) {

        Page<BoardCategory> categoryPage =
                boardCategoryRepository.findAllByCompany_Id(companyId, pageable);

        return categoryPage.map(AdminBoardResDto.Category::from);
    }
}
