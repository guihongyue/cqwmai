package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // TODO 后期需要进行md5加密，然后再进行比对
       /* password = DigestUtils.appendMd5DigestAsHex(password.getBytes());*/
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }


    /*   新增员工   */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        //对象属性拷贝  将employeeDTO中的属性拷贝到employee中去   employeeDTO里的参数是employee中的一部分，
        BeanUtils.copyProperties(employeeDTO,employee);

        //设置账号的状态，默认为正常状态为1(正常)，若为0(禁用)      StatusConstant.ENABLE是设置的常用参数为 1
        employee.setStatus(StatusConstant.ENABLE);

        //设置密码，默认密码为123456  PasswordConstant.DEFAULT_PASSWORD是设置好的参数 为 123456
        employee.setPassword(PasswordConstant.DEFAULT_PASSWORD);

        //设置当前记录的创建时间和修改时间  LocalDateTime.now()设置时间为当前时间
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());

        //设置当前记录创建人id和修改人id     将令牌出保存的id取出来存放到此处
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        //调用持久层Mapper
        employeeMapper.insert(employee);

    }



    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        //开始分页查询
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        Page<Employee> page =  employeeMapper.pageQuery(employeePageQueryDTO);

        long total = page.getTotal();
        List<Employee> records = page.getResult();

        return new PageResult(total,records);
    }

    /*      启用禁用员工账号        */
    @Override
    public void startOrStop(Integer status, Long id) {

        /*   将要修改的封装成一个实体类，这样如果下一次要修改其他的参数就可以直接用这个  提高了代码的复用性   */
        Employee employee = new Employee();
        employee.setStatus(status);
        employee.setId(id);
        /*  另一种封装实体类的方法 用bulid注解
        employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();
        */
        employeeMapper.update(employee);

    }

    /*      根据id查询员工信息      */
    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        employee.setPassword("******");
        return employee;
    }

    /*      编辑员工信息      */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        //  因为传入到Mapper层的是employee 所有要将employeeDTO转换为employee类
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);     //进行属性拷贝

        //  设置修改时间 和修改人的id
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.update(employee);
    }
}







