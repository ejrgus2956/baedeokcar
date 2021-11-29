package com.project.baedeokcar.service;

import com.project.baedeokcar.domain.dto.PostsDTO;
import com.project.baedeokcar.domain.posts.Posts;
import com.project.baedeokcar.repository.PostsRepository;
import com.project.baedeokcar.domain.dto.PostsResponseDto;
import com.project.baedeokcar.domain.dto.PostsSaveRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Log4j2
// 의존성 자동 주입
@RequiredArgsConstructor
public class PostsServiceImpl implements PostsService {

    // 반드시 final 로 선언
    private final PostsRepository postsRepository;
    private final ModelMapper modelMapper;


    private static final int PAGE_POST_COUNT = 15; // 한페이지에 존재하는 게시글 수

    // 게시글 조회
    @Override
    public PostsDTO read(Long id) {

        // modelMapper 사용
        Optional<Posts> result = postsRepository.findById(id);
        log.info("result : " + result);

        // 조회수 증가
        postsRepository.updateViewCount(id);

        return result.isPresent() ? modelMapper.map(result.get(), PostsDTO.class) : null;

    }

    @Override
    public void modify(PostsDTO postsDTO) {

        Posts posts = modelMapper.map(postsDTO, Posts.class);
        log.info("posts : " + posts);

        postsRepository.save(posts);
    }

    // 수정 필요
    @Override
    public void remove(Long id) {

        log.info("id : " + id);

        postsRepository.deleteById(id);

    }

    @Override
    public List<PostsResponseDto> findAllDesc() {
        return postsRepository.findAllDesc().stream()
                .map(PostsResponseDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public Long save(PostsSaveRequestDto postsListRequestDto) {
        return postsRepository.save(postsListRequestDto.toEntity()).getId();
    }

    @Override
    public List<PostsResponseDto> search(String keyword) {
        return postsRepository.findAllByTitleContainingOrderByIdDesc(keyword).stream()
                .map(PostsResponseDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostsResponseDto> search(String factor, String keyword) {
        List<Posts> responseDtos = null;

        if (factor.equals("title")) {
            responseDtos = postsRepository.findAllByTitleContainingOrderByIdDesc(keyword);
        } else if (factor.equals("writer")) {
            responseDtos = postsRepository.findAllByWriterContainingOrderByIdDesc(keyword);
        } else if (factor.equals("content")) {
            responseDtos = postsRepository.findAllByContentContainingOrderByIdDesc(keyword);
        }


        return responseDtos.stream()
                .map(PostsResponseDto::new)
                .collect(Collectors.toList());
    }


    @Override
    public Page<PostsResponseDto> search(String factor, String keyword, Pageable pageable) {
        Page<Posts> posts = null;

        if (factor.equals("title")) {
            posts = postsRepository.findAllByTitleContainingOrderByIdDesc(keyword, pageable);
        } else if (factor.equals("writer")) {
            posts = postsRepository.findAllByWriterContainingOrderByIdDesc(keyword, pageable);
        } else if (factor.equals("content")) {
            posts = postsRepository.findAllByContentContainingOrderByIdDesc(keyword, pageable);
        }
        Page<PostsResponseDto> dtoPage = posts.map(p -> new PostsResponseDto(p));
        return dtoPage;
    }

    // 검색 조건에 따른 동적 쿼리 생성 필요. -> QueryDsl 사용 필요 조건 등
    @Override
    public Page<PostsResponseDto> getPostsList(Pageable pageable) {
        Page<Posts> posts = postsRepository.findAll(pageable);

//      이렇게하니까 total Page 같은 부분 들이 망가짐. 페이지 정보를 유지하며 DTO로 반환하려면
//        List<PostsResponseDto> allPosts = posts.stream()
//                .map(PostsResponseDto::new)
//                .collect(Collectors.toList());
//
//        return new PageImpl<>(allPosts)

        // 이거 정말 중요 위에 내용으로 바꾸면 페이징 정보가 유지되지 않는다.
        Page<PostsResponseDto> dtoPage = posts.map(p -> new PostsResponseDto(p));
        return dtoPage;
    }

    @Override
    public Page<PostsResponseDto> getPostsList(String option, String keyword, Pageable pageable) {
//        Page<Posts> posts = postsRepository.findAll(pageable);

        Page<Posts> posts = postsRepository.findPosts(option, keyword, pageable);
//      이렇게하니까 total Page 같은 부분 들이 망가짐. 페이지 정보를 유지하며 DTO로 반환하려면
//        List<PostsResponseDto> allPosts = posts.stream()
//                .map(PostsResponseDto::new)
//                .collect(Collectors.toList());
//
//        return new PageImpl<>(allPosts)

        // 이거 정말 중요 위에 내용으로 바꾸면 페이징 정보가 유지되지 않는다.
        Page<PostsResponseDto> dtoPage = posts.map(p -> new PostsResponseDto(p));
        return dtoPage;
    }
}
