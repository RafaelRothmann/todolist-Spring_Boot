package io.github.rafaelrothmann.todolist.task;

import org.springframework.web.bind.annotation.RestController;

import io.github.rafaelrothmann.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping(value = "/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        taskModel.setIdUser((UUID) request.getAttribute("idUser"));

        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStarAt()) || currentDate.isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERRO NA DATA");
        }

        if (taskModel.getStarAt().isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A DATA DE INICO É MAIOR QUE A DATA DE FIM");
        }

        var task = this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {
        var taks = this.taskRepository.findByIdUser((UUID) request.getAttribute("idUser"));
        return taks;
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id) {

        var task = this.taskRepository.findById(id).orElse(null);

        if (task.equals(null)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("TAREFA NÃO ENCONTRADA");
        }

        var idUser = request.getAttribute("idUser");

        if(!task.getIdUser().equals(idUser)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("USUARIO TEM NÃO PERMISSÃO");
        }

        Utils.copyNonNullProperties(taskModel, task);

        taskModel.setIdUser((UUID) idUser);
        taskModel.setId(id);
        var taskFinal = this.taskRepository.save(task);
        return ResponseEntity.status(HttpStatus.OK).body(taskFinal);
    }
}
