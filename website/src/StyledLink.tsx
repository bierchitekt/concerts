import { FC, PropsWithChildren } from 'react';

type StyledLinkProps = {
    href: string;
};
export const StyledLink: FC<PropsWithChildren<StyledLinkProps>> = ({ href, children }) => {
    return (
        <a className='text-neutral-500' href={href}>
            {children}
        </a>
    );
};
